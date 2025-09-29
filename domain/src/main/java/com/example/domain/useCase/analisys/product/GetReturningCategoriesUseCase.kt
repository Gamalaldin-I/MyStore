package com.example.domain.useCase.analisys.product

import com.example.domain.model.CategorySales
import com.example.domain.repo.AnalysisRepo
import com.example.domain.util.DateHelper

class GetReturningCategoriesUseCase(private val repo: AnalysisRepo){
    suspend operator fun invoke(duration: String): List<CategorySales> {
        val (startDate, endDate) = DateHelper.giveStartAndEndDateFromToDay(duration)
        return repo.getReturningCategoriesByDate(startDate, endDate)
    }
}