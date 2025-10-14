package com.example.domain.useCase.sales

import com.example.domain.repo.SalesRepo

class GetTotalOfSalesByRageOfDaysUseCase(private val salesRepo: SalesRepo){
    suspend operator fun invoke(s:String,e:String): Double{
        return salesRepo.getTotalOfSalesByDateRange(s,e) ?: 0.0
}
}