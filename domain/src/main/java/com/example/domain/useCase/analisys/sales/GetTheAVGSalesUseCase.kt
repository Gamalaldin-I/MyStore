package com.example.domain.useCase.analisys.sales

import com.example.domain.repo.AnalysisRepo

class GetTheAVGSalesUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(period: String) =
        repo.getTheAvgOfSales(period)?:0.0
}