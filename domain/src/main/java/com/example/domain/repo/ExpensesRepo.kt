package com.example.domain.repo

import com.example.domain.model.Expense

interface ExpensesRepo {
    suspend fun insertExpense(expense: Expense): Pair<Boolean, String>
    suspend fun fetchExpenses(): Boolean
    suspend fun getAllExpenses(): List<Expense>
    suspend fun deleteExpense(id: String):Pair<Boolean, String>
    suspend fun getExpensesByDate(date: String): List<Expense>
    suspend fun getTotalOfExpensesByDateRange(start: String, end:String): Double?

}