package com.example.domain.repo

import com.example.domain.model.Expense

interface ExpensesRepo {
    suspend fun insertExpense(expense: Expense)
    suspend fun getAllExpenses(): List<Expense>
    suspend fun updateExpense(expense: Expense)
}