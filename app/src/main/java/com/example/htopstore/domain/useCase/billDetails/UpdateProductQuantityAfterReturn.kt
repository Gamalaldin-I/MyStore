package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import javax.inject.Inject

class UpdateProductQuantityAfterReturn
    @Inject constructor(private val localRpo: BillDetailsRepoImp){
    suspend operator fun invoke(returnRequest: SoldProduct) {
            localRpo.updateProductQuantityAfterReturn(returnRequest.productId!!, returnRequest.quantity)
    }
}