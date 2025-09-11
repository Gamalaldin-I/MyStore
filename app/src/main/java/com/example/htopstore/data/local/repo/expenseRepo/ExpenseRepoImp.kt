package com.example.htopstore.data.local.repo.expenseRepo

import android.content.Context
import com.example.htopstore.data.local.model.Expense
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepoImp (val context:Context): ExpenseRepo{
    val expDao = AppDataBase.getDatabase(context).expenseDao()
    override suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
        expDao.insertExpense(expense)}
    }

    override suspend fun getAllExpenses(): List<Expense> = withContext(Dispatchers.IO) {
        expDao.getAllExpenses()
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
        expDao.updateExpense(expense)
        }
    }
}