package com.example.htopstore.data.local.repo.epenseRepo

import com.example.htopstore.data.local.model.Expense

interface ExpenseRepo {
    suspend fun insertExpense(expense: Expense)
    suspend fun getAllExpenses(): List<Expense>
    suspend fun updateExpense(expense: Expense)
}