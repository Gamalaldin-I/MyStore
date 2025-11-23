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
 * Handles cart checkout, stock updates, and bill generation
 */
class SellUseCase(
    private val salesRepo: SalesRepo,
    private val billRepo: BillRepo
) {

    companion object {
        private const val TAG = "SellUseCase"
        private const val MIN_VALID_QUANTITY = 1
    }


    suspend operator fun invoke(
        cartList: List<CartProduct>,
        discount: Int = 0,
        onProgress: (Float) -> Unit
    ) {
        // Validate inputs
        validateInputs(cartList, discount)

        // Initialize operation
        val operationId = IdGenerator.generateTimestampedId()
        val currentDate = DateHelper.getCurrentDate()
        val currentTime = DateHelper.getCurrentTime()
        val timestamp = DateHelper.getCurrentTimestampTz()

        Log.d(TAG, "Starting sale: ID=$operationId, Items=${cartList.size}, Discount=$discount%")

        try {
            // Process cart items
            val soldProducts = processCartItems(
                cartList = cartList,
                operationId = operationId,
                discount = discount,
                currentDate = currentDate,
                currentTime = currentTime,
                timestamp = timestamp,
                onProgress = onProgress
            )

            // Validate processed items
            if (soldProducts.isEmpty()) {
                Log.w(TAG, "No valid products to sell after processing")
                return
            }

            // Calculate total and create bill
            val totalCash = calculateTotal(soldProducts)
            val bill = createBill(
                id = operationId,
                date = currentDate,
                time = currentTime,
                totalCash = totalCash,
                discount = discount,
                timestamp = timestamp
            )

            // Persist to database
            saveSaleTransaction(bill, soldProducts)

            Log.d(TAG, "Sale completed: ${soldProducts.size} items, Total=$totalCash")

        } catch (e: Exception) {
            Log.e(TAG, "Sale failed: ${e.message}", e)
            throw e
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

            // Update stock
            updateStock(item)

            soldProducts.add(soldProduct)

            // Update progress
            val progress = (index + 1) * progressStep
            withContext(Dispatchers.Main){
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

        val discountAmount = (price * discountPercent) / 100.0
        return price - discountAmount
    }

    private suspend fun updateStock(item: CartProduct) {
        try {
            salesRepo.updateQuantityAvailableAfterSell(item.id, item.sellingCount)
            Log.d(TAG, "Stock updated: ${item.name} (-${item.sellingCount})")
        } catch (e: Exception) {
            Log.e(TAG, "Stock update failed: ${item.name} - ${e.message}")
            throw Exception("Failed to update stock for ${item.name}", e)
        }
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

    private suspend fun saveSaleTransaction(bill: Bill, soldProducts: List<SoldProduct>) {
        try {
            billRepo.insertBill(bill)
            salesRepo.insertBillDetails(soldProducts)
            Log.d(TAG, "Transaction saved: Bill=${bill.id}, Items=${soldProducts.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Transaction save failed: ${e.message}")
            throw Exception("Failed to save transaction", e)
        }
    }
}