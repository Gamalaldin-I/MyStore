package com.example.domain.useCase.bill

import com.example.domain.repo.BillRepo
import com.example.domain.repo.SalesRepo

class FetchAllNewBillsUseCase(
    private val billRepo: BillRepo,
    private val salesRepo: SalesRepo
) {
    suspend operator fun invoke(): Pair<Boolean, String>{
        val billsResult =billRepo.fetchBills()
        val salesResult=salesRepo.fetchSalesFromRemote()

        return if (billsResult.first && salesResult.first) {
            Pair(true, "Bills and sales fetched successfully")
        } else {
            Pair(false, "Error fetching bills and sales")
        }
    }
}
