package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import javax.inject.Inject

class DeleteBillUseCase @Inject constructor(private val localRepo: BillDetailsRepoImp) {
    suspend operator fun invoke(id: String) {
        localRepo.deleteSaleById(id)
    }

}