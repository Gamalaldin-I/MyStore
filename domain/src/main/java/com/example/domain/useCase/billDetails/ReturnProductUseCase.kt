package com.example.domain.useCase.billDetails

import com.example.domain.model.SoldProduct
import com.example.domain.repo.BillDetailsRepo
import com.example.domain.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ReturnProductUseCase(
    private val localRepo: BillDetailsRepo
) {
    suspend operator fun invoke(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct
    ): String {
        val returnedItem = returnRequest.copy(
            detailId = IdGenerator.generateTimestampedId(),
            saleId = null,
            quantity = -abs(returnRequest.quantity)
        )
        return withContext(Dispatchers.IO) {
            kotlinx.coroutines.coroutineScope {
                val insertReturn = async { localRepo.insertReturn(returnedItem) }
                val productRestore = async { updateProductQuantityAfterReturn(returnRequest) }
                val cashUpdate = async { updateTotalCashOfBillAfterReturn(soldProduct, returnRequest) }

                awaitAll(insertReturn, productRestore, cashUpdate)
            }

            updateBillProduct(soldProduct, returnRequest)
        }
    }

    private suspend fun updateProductQuantityAfterReturn(returnRequest: SoldProduct) {
        localRepo.updateProductQuantityAfterReturn(
            returnRequest.productId!!,
            abs(returnRequest.quantity)
        )
    }

    private suspend fun updateTotalCashOfBillAfterReturn(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct
    ) {
        val returnValue = returnRequest.sellingPrice * abs(returnRequest.quantity)
        localRepo.updateSaleCashAfterReturn(soldProduct.saleId!!, returnValue)
    }

    private suspend fun updateBillProduct(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct
    ): String {
        val remainingQuantity = soldProduct.quantity - abs(returnRequest.quantity)

        return if (remainingQuantity <= 0) {
            localRepo.deleteSoldProduct(soldProduct)
            "Item removed from bill"
        } else {
            localRepo.updateBillProductQuantityAfterReturn(
                soldProduct.detailId,
                abs(returnRequest.quantity)
            )
            "Item updated in bill"
        }
    }
}
