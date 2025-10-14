package com.example.domain.useCase.sales

import com.example.domain.repo.SalesRepo

class GetTheTotalOfTheProfitByRangeOfDaysUseCase(private val salesRepo: SalesRepo) {
    suspend operator fun invoke(s:String,e:String): Double{
        return salesRepo.getTotalOfProfitByDateRange(s,e) ?: 0.0
}
}