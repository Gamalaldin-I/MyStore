package com.example.domain.useCase.analisys.sales

import com.example.domain.repo.AnalysisRepo

class GetProfitByPeriodUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(period: String): Double{
        return repo.getTheProfit(period)?:0.0
    }
}
