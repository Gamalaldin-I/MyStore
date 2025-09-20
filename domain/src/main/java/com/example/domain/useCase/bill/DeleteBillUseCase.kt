package com.example.domain.useCase.bill

import com.example.domain.repo.BillDetailsRepo

class DeleteBillUseCase (private val localRepo: BillDetailsRepo) {
    suspend operator fun invoke(id: String) {
        localRepo.deleteSaleById(id)
    }

}