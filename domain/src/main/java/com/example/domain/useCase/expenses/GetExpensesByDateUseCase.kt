package com.example.domain.useCase.expenses

import com.example.domain.repo.ExpensesRepo

class GetExpensesByDateUseCase(private val repo: ExpensesRepo) {
    suspend operator fun invoke(date: String) = repo.getExpensesByDate(date)
}