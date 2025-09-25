package com.example.domain.useCase.bill

import com.example.domain.repo.BillRepo

class GetBillByDateUseCase(private val repo: BillRepo){
     operator fun invoke(date:String) = repo.getBillsByDate(date)
}