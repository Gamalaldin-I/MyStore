package com.example.domain.useCase.expenses

import com.example.domain.repo.ExpensesRepo

class DeleteOutcomeUseCase(private val repo: ExpensesRepo){
    suspend operator fun invoke(id: String): Pair<Boolean, String> {
        return repo.deleteExpense(id)
    }
}