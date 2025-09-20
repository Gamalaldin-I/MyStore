package com.example.domain.useCase.sales

import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo

class GetSoldOnlyUseCase(private val salesRepo: SalesRepo) {
    suspend operator fun invoke(): List<SoldProduct> {
        return salesRepo.getSoldOnly()
    }
}
