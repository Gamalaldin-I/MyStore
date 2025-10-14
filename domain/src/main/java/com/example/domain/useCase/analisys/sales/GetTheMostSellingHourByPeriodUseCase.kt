package com.example.domain.useCase.analisys.sales

import com.example.domain.repo.AnalysisRepo

class GetTheMostSellingHourByPeriodUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(period: String) =
        repo.getTheHoursWithHighestSales(period)?:""
}
