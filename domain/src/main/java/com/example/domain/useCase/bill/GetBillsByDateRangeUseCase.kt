package com.example.domain.useCase.bill

import com.example.domain.repo.BillRepo

class GetBillsByDateRangeUseCase(private val repo: BillRepo){
    suspend operator fun invoke(since: String, to:String) =
        repo.getBillsByDateRange(since = since, to = to)
}