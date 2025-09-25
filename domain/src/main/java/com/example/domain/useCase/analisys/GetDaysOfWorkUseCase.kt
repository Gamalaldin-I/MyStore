package com.example.domain.useCase.analisys

import com.example.domain.repo.AnalysisRepo

class GetDaysOfWorkUseCase(private val repo: AnalysisRepo) {
    suspend operator  fun invoke(): List<String> {
        return repo.getTheDaysOfSales()
    }

}