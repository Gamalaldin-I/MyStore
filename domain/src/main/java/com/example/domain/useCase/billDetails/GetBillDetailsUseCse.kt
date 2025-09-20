package com.example.domain.useCase.billDetails
import com.example.domain.model.BillWithDetails
import com.example.domain.repo.BillDetailsRepo

class GetBillDetailsUseCse
    (private val localRpo: BillDetailsRepo){
    suspend operator fun invoke(id: String): BillWithDetails {
        val billWithDet = localRpo.getBillWithDetails(id)
        return billWithDet
    }
}