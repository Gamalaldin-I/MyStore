package com.example.domain.useCase.analisys

import com.example.domain.repo.AnalysisRepo

class GetSpecificDayUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(day: String): String{
        return repo.getSpecificDay(day)
    }
}