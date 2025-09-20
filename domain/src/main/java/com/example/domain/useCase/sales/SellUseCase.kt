package com.example.domain.useCase.sales

import android.util.Log
import com.example.domain.model.Bill
import com.example.domain.model.CartProduct
import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator

class SellUseCase(private val salesRepo: SalesRepo) {

    suspend operator fun invoke(cartList: List<CartProduct>, discount: Int = 0) {
        if (cartList.isEmpty()) {
            Log.d("SELL_ERROR", "Cart list is empty, cannot proceed with sale")
            return
        }

        // Initialize operation variables
        val currentOperationId = IdGenerator.generateTimestampedId()
        val currentDate = DateHelper.getCurrentDate()
        val currentTime = DateHelper.getCurrentTime()
        var totalCash = 0.0
        val soldProducts = ArrayList<SoldProduct>()

        Log.d("SELL_ERROR", "Starting sale operation with ${cartList.size} items")
        Log.d("SELL_ERROR", "Operation ID: $currentOperationId")

        try {
            // Process each item in the cart
            for ((index, item) in cartList.withIndex()) {
                Log.d("SELL_ERROR", "Processing item ${index + 1}/${cartList.size}: ${item.name}")

                if (item.sellingCount <= 0) {
                    Log.w("SELL_ERROR", "Skipping item ${item.name} - invalid quantity: ${item.sellingCount}")
                    continue
                }

                // Calculate price after discount
                val discountValue = (item.pricePerOne * discount) / 100.0
                val priceAfterDiscount = item.pricePerOne - discountValue
                val itemTotal = priceAfterDiscount * item.sellingCount

                val soldProduct = SoldProduct(
                    saleId = currentOperationId,
                    detailId = IdGenerator.generateTimestampedId(),
                    productId = item.id,
                    name = item.name,
                    type = item.type,
                    quantity = item.sellingCount,
                    price = item.buyingPrice,
                    sellingPrice = priceAfterDiscount,
                    sellDate = currentDate,
                    sellTime = currentTime
                )

                // Update stock quantity BEFORE adding to sold products
                try {
                    salesRepo.updateQuantityAvailableAfterSell(item.id, item.sellingCount)
                    Log.d("SELL_ERROR", "Updated stock for ${item.name}: -${item.sellingCount}")
                } catch (e: Exception) {
                    Log.e("SELL_ERROR", "Failed to update stock for ${item.name}: ${e.message}")
                    throw e // Re-throw to abort the entire operation
                }

                totalCash += itemTotal
                soldProducts.add(soldProduct)

                Log.d("SELL_ERROR", "Processed ${item.name}: qty=${item.sellingCount}, total=$itemTotal")
            }

            if (soldProducts.isEmpty()) {
                Log.w("SELL_ERROR", "No valid products to sell")
                return
            }

            // Create and insert bill
            val bill = Bill(
                saleId = currentOperationId,
                date = currentDate,
                time = currentTime,
                totalCash = totalCash,
                discount = discount
            )

            Log.d("SELL_ERROR", "Creating bill: Total=$totalCash, Discount=$discount%")

            // Insert bill and details
            salesRepo.insertBill(bill)
            salesRepo.insertBillDetails(soldProducts)

            Log.d("SELL_ERROR", "Sale completed successfully!")
            Log.d("SELL_ERROR", "Bill inserted with ${soldProducts.size} items")

        } catch (e: Exception) {
            Log.e("SELL_ERROR", "Error during sale operation: ${e.message}", e)
            throw e // Re-throw to let the caller handle it
        }
    }
}