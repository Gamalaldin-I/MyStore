package com.example.data.repo

import com.example.data.Mapper.toExpense
import com.example.data.Mapper.toExpenseEntity
import com.example.data.local.dao.ExpenseDao
import com.example.domain.model.Expense
import com.example.domain.repo.ExpensesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepoImp (private val expenseDao: ExpenseDao): ExpensesRepo {
    override suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.insertExpense(expense.toExpenseEntity())
        }
    }

    override suspend fun getAllExpenses(): List<Expense> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpenses().map { it.toExpense() }
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.updateExpense(expense.toExpenseEntity())
        }
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.deleteExpense(id)
    }

    override suspend fun getExpensesByDate(date: String): List<Expense> =
            expenseDao.getExpensesListByDate(date).map { it.toExpense() }

    override suspend fun getTotalOfExpensesByDateRange(
        start: String,
        end: String,
    ): Double? = expenseDao.getTheTotalOfExpensesByRange(start, end)



}