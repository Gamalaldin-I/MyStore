package com.example.domain.useCase.sales

import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo

class GetReturnsByDateUseCase(private val salesRepo: SalesRepo) {
    suspend operator fun invoke(date: String): List<SoldProduct> {
        return salesRepo.getReturnsByDate(date)
    }
}