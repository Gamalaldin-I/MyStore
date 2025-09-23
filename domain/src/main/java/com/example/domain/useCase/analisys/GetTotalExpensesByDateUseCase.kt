package com.example.domain.useCase.analisys

import com.example.domain.repo.AnalysisRepo
import kotlinx.coroutines.flow.Flow

class GetTotalExpensesByDateUseCase(private val repo: AnalysisRepo) {
     operator fun invoke(date: String): Flow<Double?> {
        return repo.getExpensesByDate(date)
    }

}