package com.example.domain.useCase.billDetails

import android.util.Log
import com.example.domain.model.SoldProduct
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.repo.BillDetailsRepo
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.util.IdGenerator
import com.example.domain.util.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Use case for processing product returns
 * Handles stock restoration, bill updates, and cash adjustments
 */
class ReturnProductUseCase(
    private val billDetailsRepo: BillDetailsRepo,
    private val staffRepo: StaffRepo,
    private val notificationSender: InsertNotificationUseCase
) {

    companion object {
        private const val TAG = "ReturnProductUseCase"
    }

    sealed class ReturnResult {
        data class Success(val message: String) : ReturnResult()
        data class Error(val message: String) : ReturnResult()
    }

    suspend operator fun invoke(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        user:User,
        store:Store
    ): ReturnResult = withContext(Dispatchers.IO) {

        try {

            /////////////////////////////before operation/////////////////////////////////
            val resOfGo = staffRepo.preformAction()
            if(!resOfGo.first){
                ReturnResult.Error(resOfGo.second)
            }

            Log.d(TAG, "Processing return: ${soldProduct.name} x${returnRequest.quantity}")

            // Validate return request
            validateReturnRequest(soldProduct, returnRequest)

            // Process the return
            processReturn(soldProduct, returnRequest,user,store)

        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Validation failed: ${e.message}")
            ReturnResult.Error(e.message ?: "Invalid return request")
        } catch (e: Exception) {
            Log.e(TAG, "Return failed: ${e.message}", e)
            ReturnResult.Error("Failed to process return")
        }
    }

    // ==================== Validation ====================

    private fun validateReturnRequest(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct
    ) {
        // Check if product is in stock
        require(soldProduct.productId != null) {
            Log.d(
                TAG,"Product ID is null, returns are not allowed ${soldProduct.productId?:"not found"}"
            )
            "Product is not in stock, returns are not allowed"
        }

        // Check if product and bill IDs match
        require(soldProduct.productId == returnRequest.productId) {
            "Product ID mismatch"
        }

        require(soldProduct.billId != null) {
            "Invalid bill ID"
        }

        // Validate return quantity
        val returnQuantity = abs(returnRequest.quantity)
        require(returnQuantity > 0) {
            "Return quantity must be greater than 0"
        }

        require(returnQuantity <= soldProduct.quantity) {
            "Cannot return more than sold quantity (max: ${soldProduct.quantity})"
        }

        Log.d(TAG, "Validation passed: returning $returnQuantity of ${soldProduct.quantity}")
    }

    // ==================== Return Processing ====================

    private suspend fun processReturn(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        user: User,
        store: Store
    ): ReturnResult {
        val returnQuantity = abs(returnRequest.quantity)

        // Create return record
        val returnedItem = createReturnRecord(returnRequest, returnQuantity)

        // Execute return operations in parallel
        executeReturnOperations(soldProduct, returnRequest, returnedItem, returnQuantity)

        // Update bill product
        val updateMessage = updateBillProduct(soldProduct, returnQuantity)

        Log.d(TAG, "Return completed: $updateMessage")
        val notification = NotificationManager.createUpdateBillNotification(
            user =user,
            storeId = store.id,
            billId = soldProduct.billId!!,
            returnRequest)
        notificationSender(notification)

        return ReturnResult.Success(updateMessage)
    }

    private fun createReturnRecord(
        returnRequest: SoldProduct,
        returnQuantity: Int
    ): SoldProduct {
        return returnRequest.copy(
            id = IdGenerator.generateTimestampedId(),
            billId = null,
            quantity = -returnQuantity // Negative quantity indicates return
        )
    }

    private suspend fun executeReturnOperations(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        returnedItem: SoldProduct,
        returnQuantity: Int
    ) {
        kotlinx.coroutines.coroutineScope {
            val operations = listOf(
                async { insertReturnRecord(returnedItem) },
                async { restoreProductStock(returnRequest.productId!!, returnQuantity) },
                async { adjustBillCash(soldProduct, returnRequest, returnQuantity) }
            )

            awaitAll(*operations.toTypedArray())
        }
    }

    // ==================== Individual Operations ====================

    private suspend fun insertReturnRecord(returnedItem: SoldProduct) {
        try {
            val (success,msg)=billDetailsRepo.insertReturn(returnedItem)
            if(success) {
                Log.d(TAG, "Return record inserted: ${returnedItem.name}")
            }else{
                Log.e(TAG, "Failed to insert return record: $msg")
                throw Exception("Failed to record return", Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert return record: ${e.message}")
            throw Exception("Failed to record return", e)
        }
    }

    private suspend fun restoreProductStock(productId: String, quantity: Int) {
        try {
            val( success,msg)=billDetailsRepo.updateProductQuantityAfterReturn(productId, quantity)
            if(!success){
                Log.e(TAG, "Failed to restore stock: $msg")
                throw Exception("Failed to restore product stock", Exception(msg))
            }
            Log.d(TAG, "Stock restored: +$quantity")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore stock: ${e.message}")
            throw Exception("Failed to restore product stock", e)
        }
    }

    private suspend fun adjustBillCash(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        returnQuantity: Int
    ) {
        try {
            val refundAmount = calculateRefundAmount(returnRequest.sellingPrice, returnQuantity)
            val(success,msg)=billDetailsRepo.updateSaleCashAfterReturn(soldProduct.billId!!, refundAmount)
            if(success){
                Log.d(TAG, "Bill cash adjusted: -$refundAmount")
                return
            }
            else{
                Log.e(TAG, "Failed to adjust bill cash: $msg")
                throw Exception("Failed to adjust bill amount", Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to adjust bill cash: ${e.message}")
            throw Exception("Failed to adjust bill amount", e)
        }
    }

    private fun calculateRefundAmount(sellingPrice: Double, quantity: Int): Double {
        return sellingPrice * quantity
    }

    // ==================== Bill Product Update ====================

    private suspend fun updateBillProduct(
        soldProduct: SoldProduct,
        returnQuantity: Int
    ): String {
        val remainingQuantity = soldProduct.quantity - returnQuantity

        return if (remainingQuantity <= 0) {
            removeBillProduct(soldProduct)
        } else {
            updateBillProductQuantity(soldProduct, returnQuantity, remainingQuantity)
        }
    }

    private suspend fun removeBillProduct(soldProduct: SoldProduct): String {
        try {
            val (success, msg) = billDetailsRepo.deleteSoldProduct(soldProduct)
            if(success) {
                Log.d(TAG, "Product removed from bill: ${soldProduct.name}")
                return "Product fully returned"
            } else {
                Log.e(TAG, "Failed to remove product from bill: $msg")
                throw Exception("Failed to remove product from bill: $msg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove product from bill: ${e.message}")
            throw Exception("Failed to update bill", e)
        }
    }

    private suspend fun updateBillProductQuantity(
        soldProduct: SoldProduct,
        returnedQuantity: Int,
        remainingQuantity: Int
    ): String {
        try {
            val (success, msg) = billDetailsRepo.updateBillProductQuantityAfterReturn(
                soldProduct.id,
                returnedQuantity
            )
            if(success) {
                Log.d(TAG, "Bill product updated: ${soldProduct.name} (remaining: $remainingQuantity)")
                return "Partial return processed ($remainingQuantity remaining)"
            } else {
                Log.e(TAG, "Failed to update bill product: $msg")
                throw Exception("Failed to update bill product: $msg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update bill product: ${e.message}")
            throw Exception("Failed to update bill", e)
        }
    }
}