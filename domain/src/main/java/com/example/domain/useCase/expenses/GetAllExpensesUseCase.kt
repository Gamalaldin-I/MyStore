package com.example.domain.useCase.expenses

import com.example.domain.model.Expense
import com.example.domain.repo.ExpensesRepo

class GetAllExpensesUseCase(private val repo: ExpensesRepo){
    suspend operator fun invoke():List<Expense> = repo.getAllExpenses()
}