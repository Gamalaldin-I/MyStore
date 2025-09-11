package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import com.example.htopstore.util.IdGenerator
import javax.inject.Inject

class InsertReturnProduct
@Inject constructor(private val localRpo: BillDetailsRepoImp) {
    suspend operator fun invoke(returnRequest: SoldProduct){
        val returnedItem = returnRequest.copy(
            detailId = IdGenerator.generateTimestampedId(),
            saleId = null,
            quantity = -returnRequest.quantity
        )
        localRpo.insertReturn(returnedItem)
    }

}