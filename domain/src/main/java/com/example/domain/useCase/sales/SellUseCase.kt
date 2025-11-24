package com.example.domain.useCase.sales

import android.util.Log
import com.example.domain.model.Bill
import com.example.domain.model.CartProduct
import com.example.domain.model.SoldProduct
import com.example.domain.repo.BillRepo
import com.example.domain.repo.SalesRepo
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for processing sales transactions
 * Handles cart checkout, stock updates, and bill generation with automatic retry mechanism
 */
class SellUseCase(
    private val salesRepo: SalesRepo,
    private val billRepo: BillRepo
){
    companion object {
        private const val TAG = "SellUseCase"
        private const val MIN_VALID_QUANTITY = 1
    }

    /**
     * Process a sale transaction
     * @param cartList List of products in cart
     * @param discount Discount percentage (0-100)
     * @param billInserted Whether bill was already inserted (for retry operations)
     * @param soldItemsInserted Whether sold items were already inserted (for retry operations)
     * @param onProgress Progress callback (0-100)
     * @throws IllegalArgumentException if inputs are invalid
     * @throws Exception if transaction fails (will create pending action for retry)
     */
    suspend operator fun invoke(
        id:Int = -1,
        cartList: List<CartProduct>,
        discount: Int = 0,
        billInserted: Boolean = false,
        soldItemsInserted: Boolean = false,
        onProgress: (Float) -> Unit,
    ):String{
        // Validate inputs early
        validateInputs(cartList, discount)

        // Initialize transaction metadata
        val operationId = IdGenerator.generateTimestampedId()
        val currentDate = DateHelper.getCurrentDate()
        val currentTime = DateHelper.getCurrentTime()
        val timestamp = DateHelper.getCurrentTimestampTz()
        val actionId = id

        Log.d(TAG, "Starting sale: ID=$operationId, Items=${cartList.size}, Discount=$discount%")

        // Track completion state for retry logic
        var isBillInserted = billInserted
        var areSoldItemsInserted = soldItemsInserted
        var updatedCartList = cartList

        try {
            // Step 1: Process cart items and update stock
            val soldProducts = processCartItems(
                cartList = cartList,
                operationId = operationId,
                discount = discount,
                currentDate = currentDate,
                currentTime = currentTime,
                timestamp = timestamp,
                onProgress = onProgress
            )

            // Update the cart list with stock update status
            updatedCartList = cartList.map { it.copy() }

            if (soldProducts.isEmpty()) {
                Log.w(TAG, "No valid products to sell after processing")
                return "No valid products to sell after processing"
            }
            onProgress(80f)
            // Step 2: Calculate total and create bill
            val totalCash = calculateTotal(soldProducts)
            val bill = createBill(
                id = operationId,
                date = currentDate,
                time = currentTime,
                totalCash = totalCash,
                discount = discount,
                timestamp = timestamp
            )
            // Step 3: Insert bill if not already done
            if (!isBillInserted) {
                isBillInserted = billRepo.insertBill(bill).first
                if (!isBillInserted) {
                    throw Exception("Failed to insert bill")
                }
                onProgress(90f)
                Log.d(TAG, "Bill inserted: ID=${bill.id}")
            }

            // Step 4: Insert sold items if not already done
            if (!areSoldItemsInserted) {
                areSoldItemsInserted = salesRepo.insertBillDetails(soldProducts).first
                if (!areSoldItemsInserted) {
                    throw Exception("Failed to insert sold items")
                }
                onProgress(100f)
                Log.d(TAG, "Sold items inserted: Count=${soldProducts.size}")
            }

            Log.d(TAG, "Sale completed successfully: ${soldProducts.size} items, Total=$totalCash")
            if(actionId!=-1){
                salesRepo.updatePendingSellAction(
                    id = actionId,
                    status = "Approved",
                    soldProducts = updatedCartList,
                    discount = discount,
                    billInserted = true,
                    soldItemsInserted = true
                )
            }
            return "Sale completed successfully"

        } catch (e: Exception) {
            Log.e(TAG, "Sale failed at stage - Bill inserted: $isBillInserted, Items inserted: $areSoldItemsInserted", e)

            // Create pending action for retry
            try {
                salesRepo.insertPendingSellAction(
                    cartList = updatedCartList,
                    discount = discount,
                    billInserted = isBillInserted,
                    soldItemsInserted = areSoldItemsInserted
                )
                Log.d(TAG, "Pending action created for retry - Operation ID: $operationId")
                return "Pending action created for retry"
            } catch (pendingException: Exception) {
                Log.e(TAG, "Critical: Failed to create pending action", pendingException)
                // Re-throw original exception with context
                throw Exception("Sale failed and could not create retry action: ${e.message}", e)
            }
            // Re-throw original exception
            throw Exception("Sale transaction failed: ${e.message}", e)
        }
    }

    // ==================== Private Helper Methods ====================

    private fun validateInputs(cartList: List<CartProduct>, discount: Int) {
        require(cartList.isNotEmpty()) {
            "Cart cannot be empty"
        }
        require(discount in 0..100) {
            "Discount must be between 0 and 100"
        }
        require(cartList.all { it.sellingCount >= MIN_VALID_QUANTITY }) {
            "All cart items must have valid quantities (>= $MIN_VALID_QUANTITY)"
        }
    }

    private suspend fun processCartItems(
        cartList: List<CartProduct>,
        operationId: String,
        discount: Int,
        currentDate: String,
        currentTime: String,
        timestamp: String,
        onProgress: (Float) -> Unit
    ): List<SoldProduct> {
        val soldProducts = mutableListOf<SoldProduct>()
        val progressStep = 100f / cartList.size

        cartList.forEachIndexed { index, item ->
            Log.d(TAG, "Processing [${index + 1}/${cartList.size}]: ${item.name}")

            // Skip invalid items
            if (!isValidCartItem(item)) {
                Log.w(TAG, "Skipping invalid item: ${item.name} (qty=${item.sellingCount})")
                return@forEachIndexed
            }

            // Create sold product
            val soldProduct = createSoldProduct(
                item = item,
                billId = operationId,
                discount = discount,
                sellDate = currentDate,
                sellTime = currentTime,
                timestamp = timestamp
            )

            // Update stock if not already updated
            if (!item.stockUpdated) {
                updateStock(item)
            }

            soldProducts.add(soldProduct)

            // Update progress on main thread
            val progress = (index + 1) * progressStep
            withContext(Dispatchers.Main) {
                onProgress(progress)
            }

            Log.d(TAG, "Processed: ${item.name} x${item.sellingCount} = ${soldProduct.sellingPrice * item.sellingCount}")
        }

        return soldProducts
    }

    private fun isValidCartItem(item: CartProduct): Boolean {
        return item.sellingCount >= MIN_VALID_QUANTITY
    }

    private fun createSoldProduct(
        item: CartProduct,
        billId: String,
        discount: Int,
        sellDate: String,
        sellTime: String,
        timestamp: String
    ): SoldProduct {
        val priceAfterDiscount = calculatePriceAfterDiscount(item.pricePerOne, discount)

        return SoldProduct(
            billId = billId,
            id = IdGenerator.generateTimestampedId(),
            productId = item.id,
            name = item.name,
            type = item.type,
            quantity = item.sellingCount,
            price = item.buyingPrice,
            sellingPrice = priceAfterDiscount,
            sellDate = sellDate,
            sellTime = sellTime,
            lastUpdate = timestamp,
            deleted = false
        )
    }

    private fun calculatePriceAfterDiscount(price: Double, discountPercent: Int): Double {
        if (discountPercent <= 0) return price
        return price * (1 - discountPercent / 100.0)
    }

    private suspend fun updateStock(item: CartProduct) {
        val result = salesRepo.updateQuantityAvailableAfterSell(item.id, item.sellingCount).first
       /* if (!result) {
            throw Exception("Failed to update stock for ${item.name}: $msg")
        }*/
        item.stockUpdated = result
        Log.d(TAG, "Stock updated: ${item.name} (-${item.sellingCount})")
    }

    private fun calculateTotal(soldProducts: List<SoldProduct>): Double {
        return soldProducts.sumOf { it.sellingPrice * it.quantity }
    }

    private fun createBill(
        id: String,
        date: String,
        time: String,
        totalCash: Double,
        discount: Int,
        timestamp: String
    ): Bill {
        return Bill(
            id = id,
            date = date,
            time = time,
            totalCash = totalCash,
            discount = discount,
            lastUpdate = timestamp,
            storeId = "",
            userId = "",
            deleted = false
        )
    }
}