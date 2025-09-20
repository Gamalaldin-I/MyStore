package com.example.domain.useCase.bill

import com.example.domain.model.Bill
import com.example.domain.repo.BillRepo

class GetBillsTillDateUseCase(private val repo: BillRepo){
    suspend operator fun invoke(date:String) = repo.getBillsTillDate(date)
}