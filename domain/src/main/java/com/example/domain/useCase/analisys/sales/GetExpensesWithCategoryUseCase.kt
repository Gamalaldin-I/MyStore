package com.example.domain.useCase.analisys.sales

import com.example.domain.model.ExpensesWithCategory
import com.example.domain.repo.AnalysisRepo
import com.example.domain.util.DateHelper

class GetExpensesWithCategoryUseCase(private val repo: AnalysisRepo) {
    suspend operator fun invoke(date:String): List<ExpensesWithCategory>{
        val (s,e) = DateHelper.giveStartAndEndDateFromToDay(date)
        return repo.getExpensesListByPeriod(s,e)?:emptyList()
    }
}