package com.example.domain.useCase.sales

import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo

class GetReturnsUseCase(private val salesRepo: SalesRepo) {
    suspend operator fun invoke(): List<SoldProduct> {
        return salesRepo.getReturns()
    }
}