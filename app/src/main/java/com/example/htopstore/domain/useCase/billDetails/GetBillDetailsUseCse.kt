package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import javax.inject.Inject

class GetBillDetailsUseCse
    @Inject constructor(private val localRpo: BillDetailsRepoImp){
    suspend operator fun invoke(id: String): SalesOpsWithDetails {
        val sellOp = localRpo.getBillWithDetails(id)
        return sellOp
    }
}