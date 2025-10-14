package com.example.domain.useCase.analisys.sales

import com.example.domain.repo.AnalysisRepo

class GetTheTotalSalesUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(period: String) =
        repo.getTheTotalSales(period) ?: 0.0
}
