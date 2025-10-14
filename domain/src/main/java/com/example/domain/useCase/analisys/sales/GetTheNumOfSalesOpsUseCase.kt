package com.example.domain.useCase.analisys.sales

import com.example.domain.repo.AnalysisRepo

class GetTheNumOfSalesOpsUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(period: String):Int =
        repo.getNumberOfSales(period)?:0
}
