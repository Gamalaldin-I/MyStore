package com.example.domain.useCase.bill

import com.example.domain.repo.BillRepo

class GetAllBillsUseCase(private val repo: BillRepo){
    suspend operator fun invoke() = repo.getAllBills()
}