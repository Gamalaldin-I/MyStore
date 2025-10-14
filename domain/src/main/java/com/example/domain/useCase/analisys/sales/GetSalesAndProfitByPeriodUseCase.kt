package com.example.domain.useCase.analisys.sales

import com.example.domain.model.SalesProfitByPeriod
import com.example.domain.repo.AnalysisRepo

class GetSalesAndProfitByPeriodUseCase(private val repo: AnalysisRepo) {
    suspend operator fun invoke(period: String): List<SalesProfitByPeriod> {
        return repo.getTheSalesAndProfitGroupedByPeriod(period)?:emptyList()
    }
}