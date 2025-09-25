package com.example.domain.useCase.sales

import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo
import kotlinx.coroutines.flow.Flow

class GetReturnsByDateUseCase(private val salesRepo: SalesRepo) {
     operator fun invoke(date: String): Flow<List<SoldProduct>> {
        return salesRepo.getReturnsByDate(date)
    }
}