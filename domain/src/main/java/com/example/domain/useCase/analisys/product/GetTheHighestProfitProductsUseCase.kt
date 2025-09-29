package com.example.domain.useCase.analisys.product

import com.example.domain.model.Product
import com.example.domain.repo.AnalysisRepo

class GetTheHighestProfitProductsUseCase(private val repo: AnalysisRepo) {
    suspend operator fun invoke(): List<Product> {
        return repo.getTheProductsWithHighestProfit()
    }
}