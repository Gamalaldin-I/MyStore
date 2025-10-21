package com.example.data.remote

import com.example.domain.model.Expense

interface RemoteExpensesRepo {
    fun addExpense(expense: Expense)
    fun deleteExpenseById(id: String)
    fun getExpenses(): List<Expense>
    fun addListOfExpenses(expenses: List<Expense>)

}