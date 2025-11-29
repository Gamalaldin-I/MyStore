package com.example.domain.useCase.expenses

import com.example.domain.model.Expense
import com.example.domain.repo.ExpensesRepo

data class InsertNewExpenseUseCase(private val repo: ExpensesRepo){
    suspend operator fun invoke(expense: Expense) =
        repo.insertExpense(expense)


}
