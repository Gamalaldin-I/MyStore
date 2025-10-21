package com.example.domain.useCase.expenses

import com.example.domain.repo.ExpensesRepo

class GetTotalOfExpensesByRangeOfDateUseCase(private val repo: ExpensesRepo) {
    suspend operator fun invoke(s:String,e:String): Double{
        return repo.getTotalOfExpensesByDateRange(s,e) ?: 0.0
    }
}