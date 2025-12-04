package com.example.domain.useCase.sales

import android.util.Log
import com.example.domain.model.Bill
import com.example.domain.model.CartProduct
import com.example.domain.model.PendingSellAction
import com.example.domain.model.SoldProduct
import com.example.domain.repo.BillRepo
import com.example.domain.repo.SalesRepo
import com.example.domain.useCase.pendingSellActions.AddSellPendingActionUseCase
import com.example.domain.useCase.pendingSellActions.UpdateSellActionUseCase
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clean and corrected SellUseCase (offline-safe, pending only on failure)
 *
 * Behavior:
 *  - Generates (or re-uses) billId
 *  - Steps:
 *     1) update stock for each cart item (progress 0..50)
 *     2) insert bill (progress 50..80)
 *     3) insert sold items (progress 80..100)
 *  - On any exception -> create PendingSellAction (only on failure) containing:
 *       billId, soldProducts snapshot, progress, billInserted flag, soldItemsInserted flag
 *  - If this invocation is for an existing pending action (id != -1), and success -> mark it Approved
 */
class SellUseCase(
    private val salesRepo: SalesRepo,
    private val billRepo: BillRepo,
    private val addSellPendingActionUseCase: AddSellPendingActionUseCase,
    private val updateSellActionUseCase: UpdateSellActionUseCase
) {
    companion object {
        private const val TAG = "SellUseCaseClean"
        private const val MIN_VALID_QUANTITY = 1
    }

    // last known progress (0..100)
    private var lastProgress = 0f

    /**
     * Main entry
     *
     * id: existing pending action id if retrying (-1 otherwise)
     * billId: existing billId if retrying (empty -> generate new)
     * cartList: list of CartProduct (we will take snapshot)
     * discount: 0..100
     * billInserted / soldItemsInserted: flags when retrying from partial state
     * onProgress: (0..100) callback (runs on Main thread internally)
     */
    suspend operator fun invoke(
        id: Int = -1,
        billId: String = "",
        cartList: List<CartProduct>,
        discount: Int = 0,
        billInserted: Boolean = false,
        soldItemsInserted: Boolean = false,
        onProgress: (Float) -> Unit
    ): String {
        validateInputs(cartList, discount)

        val operationId = if (billId.isNotEmpty()) billId else IdGenerator.generateTimestampedId()
        val nowDate = DateHelper.getCurrentDate()
        val nowTime = DateHelper.getCurrentTime()
        val nowTimestamp = DateHelper.getCurrentTimestampTz()
        val pendingActionId = id

        Log.d(TAG, "Sell start: opId=$operationId items=${cartList.size} discount=$discount")

        // Snapshot of cart for safety (and for saving into Pending if needed)
        val cartSnapshot = cartList.map { it.copy() }

        // progress segmentation
        val stockEnd = 50f
        val billEnd = 80f
        val itemsEnd = 100f

        // track what succeeded so far (for pending)
        var localBillInserted = billInserted
        var localSoldItemsInserted = soldItemsInserted

        try {
            // 1) Update stock & build soldProducts (progress 0..stockEnd)
            val soldProducts = processItemsUpdateStock(
                cart = cartSnapshot,
                operationId = operationId,
                discount = discount,
                date = nowDate,
                time = nowTime,
                timestamp = nowTimestamp,
                stockProgressEnd = stockEnd,
                onProgress = onProgress
            )

            if (soldProducts.isEmpty()) {
                Log.w(TAG, "No valid products to sell.")
                reportProgress(0f, onProgress)
                return "No valid products to sell"
            }

            // 2) Insert bill (progress stockEnd..billEnd)
            val total = calculateTotal(soldProducts)
            val bill = createBill(operationId, nowDate, nowTime, total, discount, nowTimestamp)

            if (!localBillInserted) {
                val billResult = withContext(Dispatchers.IO) {
                    billRepo.insertBill(bill).first
                }
                localBillInserted = billResult
                if (!localBillInserted) throw Exception("Failed to insert bill")
                // map to middle progress
                reportProgress(billEnd, onProgress)
                Log.d(TAG, "Bill inserted id=${bill.id}")
            } else {
                // already inserted in previous attempt
                reportProgress(billEnd, onProgress)
                Log.d(TAG, "Bill already inserted (retry path).")
            }

            // 3) Insert sold items (progress billEnd..itemsEnd)
            if (!localSoldItemsInserted) {
                val itemsResult = withContext(Dispatchers.IO) {
                    salesRepo.insertBillDetails(soldProducts).first
                }
                localSoldItemsInserted = itemsResult
                if (!localSoldItemsInserted) throw Exception("Failed to insert sold items")
                reportProgress(itemsEnd, onProgress)
                Log.d(TAG, "Sold items inserted count=${soldProducts.size}")
            } else {
                reportProgress(itemsEnd, onProgress)
                Log.d(TAG, "Sold items already inserted (retry path).")
            }

            // Success: if we were retrying an existing pending action, mark it approved
            if (pendingActionId != -1) {
                try {
                    updateSellActionUseCase(
                        PendingSellAction(
                            id = pendingActionId,
                            billId = operationId,
                            status = "Approved",
                            soldProducts = cartSnapshot,
                            discount = discount,
                            progress = 100,
                            billInserted = true,
                            soldItemsInserted = true
                        )
                    )
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to mark pending action approved: ${t.message}")
                }
            }

            Log.d(TAG, "Sale completed successfully. total=$total")
            return Constants.SELL_COMPLETED_MESSAGE

        } catch (e: Exception) {
            Log.e(TAG, "Sale failed (billInserted=$localBillInserted, itemsInserted=$localSoldItemsInserted)", e)

            // create pending action only on failure
            try {
                val pending = PendingSellAction(
                    id = if (pendingActionId != -1) pendingActionId else 0,
                    billId = operationId,
                    status = "Pending",
                    soldProducts = cartSnapshot,
                    discount = discount,
                    progress = lastProgress.coerceIn(0f, 100f).toInt(),
                    billInserted = localBillInserted,
                    soldItemsInserted = localSoldItemsInserted
                )

                withContext(Dispatchers.IO) {
                    addSellPendingActionUseCase(pending)
                }

                Log.d(TAG, "PendingSellAction created for op=$operationId progress=${pending.progress}")
                return Constants.SELL_FAILED_MESSAGE
            } catch (pendingEx: Exception) {
                Log.e(TAG, "Failed to create pending action", pendingEx)
                // if even creating pending fails, rethrow original error for caller to handle
                throw Exception("Sale failed and pending creation also failed: ${e.message}", e)
            }
        }
    }

    // ----------------- helpers -----------------

    private fun validateInputs(cartList: List<CartProduct>, discount: Int) {
        require(cartList.isNotEmpty()) { "Cart cannot be empty" }
        require(discount in 0..100) { "Discount must be between 0 and 100" }
        require(cartList.all { it.sellingCount >= MIN_VALID_QUANTITY }) {
            "All cart items must have valid quantities (>= $MIN_VALID_QUANTITY)"
        }
    }

    /**
     * Processes items, updates stock and returns list of SoldProduct.
     * Reports progress mapped to 0..stockProgressEnd (value provided by caller).
     */
    private suspend fun processItemsUpdateStock(
        cart: List<CartProduct>,
        operationId: String,
        discount: Int,
        date: String,
        time: String,
        timestamp: String,
        stockProgressEnd: Float,
        onProgress: (Float) -> Unit
    ): List<SoldProduct> {
        val sold = mutableListOf<SoldProduct>()
        if (cart.isEmpty()) return sold

        val total = cart.size
        cart.forEachIndexed { index, item ->
            if (!isValidCartItem(item)) {
                Log.w(TAG, "Skipping invalid item: ${item.name} qty=${item.sellingCount}")
                return@forEachIndexed
            }

            // create sold product
            val sp = createSoldProduct(item, operationId, discount, date, time, timestamp)

            // update stock if needed
            if (!item.stockUpdated) {
                val ok = withContext(Dispatchers.IO) {
                    salesRepo.updateQuantityAvailableAfterSell(item.id, item.sellingCount).first
                }
                item.stockUpdated = ok
                if (!ok) {
                    throw Exception("Failed to update stock for ${item.name}")
                }
                Log.d(TAG, "Stock updated for ${item.name} (-${item.sellingCount})")
            }

            sold.add(sp)

            // fraction across items (0..1)
            val fraction = (index + 1).toFloat() / total.toFloat()
            // map fraction to 0..stockProgressEnd
            val mappedProgress = fraction * stockProgressEnd
            reportProgress(mappedProgress, onProgress)

            // update lastProgress tracker
            lastProgress = mappedProgress
        }

        return sold
    }

    private fun isValidCartItem(item: CartProduct): Boolean = item.sellingCount >= MIN_VALID_QUANTITY

    private fun createSoldProduct(
        item: CartProduct,
        billId: String,
        discount: Int,
        sellDate: String,
        sellTime: String,
        timestamp: String
    ): SoldProduct {
        val sellingPrice = calculatePriceAfterDiscount(item.pricePerOne, discount)
        return SoldProduct(
            billId = billId,
            id = IdGenerator.generateTimestampedId(),
            productId = item.id,
            name = item.name,
            type = item.type,
            quantity = item.sellingCount,
            price = item.buyingPrice,
            sellingPrice = sellingPrice,
            sellDate = sellDate,
            sellTime = sellTime,
            lastUpdate = timestamp,
            deleted = false,
            storeId = "",
            userId =  ""
        )
    }

    private fun calculatePriceAfterDiscount(price: Double, discountPercent: Int): Double {
        if (discountPercent <= 0) return price
        return price * (1 - discountPercent / 100.0)
    }

    private fun calculateTotal(soldProducts: List<SoldProduct>): Double {
        return soldProducts.sumOf { it.sellingPrice * it.quantity }
    }

    /**
     * Reports progress on Main dispatcher and updates lastProgress.
     * Expects value 0..100
     */
    private suspend fun reportProgress(value: Float, onProgress: (Float) -> Unit) {
        lastProgress = value.coerceIn(0f, 100f)
        withContext(Dispatchers.Main) {
            try {
                onProgress(lastProgress)
            } catch (t: Throwable) {
                Log.w(TAG, "onProgress callback failed: ${t.message}")
            }
        }
    }
    private fun createBill(
        id: String,
        date: String,
        time: String,
        total: Double,
        discount: Int,
        timestamp: String
    ):Bill{
        val newBill = Bill(
            id = id,
            storeId ="",
            userId = "",
            date = date,
            time = time,
            totalCash = total,
            discount = discount,
            deleted = false,
            lastUpdate = timestamp
        )
        return newBill
    }
}
