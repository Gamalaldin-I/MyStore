package com.example.domain.useCase.analisys.product

import com.example.domain.repo.AnalysisRepo
import com.example.domain.util.DateHelper

class GetTheLeastSellingCategoryUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(duration: String): String {
        val (startDate, endDate) = DateHelper.giveStartAndEndDateFromToDay(duration)
        return repo.getTheLeastSellingCategory(startDate, endDate)
    }
}