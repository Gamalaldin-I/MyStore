package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import javax.inject.Inject

class UpdateSoldProductQuantityAfterReturn
    @Inject constructor(private val localRpo: BillDetailsRepoImp){
    suspend operator fun invoke(soldProduct: SoldProduct,
        returnRequest: SoldProduct
        ): String  {
            val flagToDelete = soldProduct.quantity - returnRequest.quantity == 0
            return if (flagToDelete) {
                localRpo.deleteSoldProduct(soldProduct)
                 "Item removed from bill"
            } else {
                localRpo.updateBillProductQuantityAfterReturn(
                    soldProduct.detailId,
                    returnRequest.quantity
                )
                "Item updated in bill"
            }
}
}