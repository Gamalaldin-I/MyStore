package com.example.domain.useCase.bill

import com.example.domain.repo.BillRepo

class GetBillByDateUseCase(private val repo: BillRepo){
    suspend operator fun invoke(date:String) = repo.getBillsByDate(date)
}