package com.example.domain.useCase.analisys.product

import com.example.domain.model.Product
import com.example.domain.repo.AnalysisRepo
import kotlinx.coroutines.flow.Flow

class GetTop5UseCase(private val repo: AnalysisRepo) {
     operator fun invoke(): Flow<List<Product>> {
        return repo.getTop10InSales()
    }
}