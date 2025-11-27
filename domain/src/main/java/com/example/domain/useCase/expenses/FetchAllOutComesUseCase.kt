package com.example.domain.useCase.expenses

import com.example.domain.repo.ExpensesRepo

class FetchAllOutComesUseCase(private val repo: ExpensesRepo){
    suspend operator fun invoke() = repo.fetchExpenses()
}