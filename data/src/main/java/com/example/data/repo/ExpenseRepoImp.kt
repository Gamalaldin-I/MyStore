package com.example.data.repo

import com.example.data.Mapper.toExpense
import com.example.data.Mapper.toExpenseEntity
import com.example.data.local.dao.ExpenseDao
import com.example.data.remote.repo.RemoteExpensesRepo
import com.example.domain.model.Expense
import com.example.domain.repo.ExpensesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepoImp (
    private val expenseDao: ExpenseDao,
    private val remote: RemoteExpensesRepo
): ExpensesRepo {
    override suspend fun insertExpense(expense: Expense): Pair<Boolean, String> =
        remote.addExpense(expense){
            expenseDao.insertExpense(expense.toExpenseEntity())
        }

    override suspend fun fetchExpenses(): Boolean {
        return remote.getExpenses { list ->
            list.forEach { outcome ->
                if(outcome.deleted){
                    expenseDao.deleteExpense(outcome.id)
                }else{
                    expenseDao.insertExpense(outcome.toExpenseEntity())
                }
            }
        }
    }


    override suspend fun getAllExpenses(): List<Expense> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpenses().map { it.toExpense() }
    }


    override suspend fun deleteExpense(id: String) =
        remote.deleteExpenseById(id){
            expenseDao.deleteExpense(id)
        }


    override suspend fun getExpensesByDate(date: String): List<Expense> =
            expenseDao.getExpensesListByDate(date).map { it.toExpense() }

    override suspend fun getTotalOfExpensesByDateRange(
        start: String,
        end: String,
    ): Double? = expenseDao.getTheTotalOfExpensesByRange(start, end)



}