
package com.example.htopstore.data.local.repo.home

import android.content.Context
import com.example.htopstore.data.local.model.Expense
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepoImp(context: Context): HomeRepo {

    private val productDao = AppDataBase.getDatabase(context).productDao()
    private val expenseDao = AppDataBase.getDatabase(context).expenseDao()
    private val salesDao = AppDataBase.getDatabase(context).salesDao()


    override suspend fun getTop5InSales(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getTop5InSales()
        }

    override suspend fun getLowStock(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getLowStock()
        }


    override suspend fun getExpensesToday(date: String): Double? =
        withContext(Dispatchers.IO) {
            expenseDao.getExpensesToday(date)
        }

    override suspend fun getIncomeToday(date: String): Double? =
        withContext(Dispatchers.IO) {
            salesDao.getTotalSalesOfToday(date)
        }

    override suspend fun getProfitToday(date: String): Double? =
        withContext(Dispatchers.IO) {
            salesDao.getProfitOfToday(date)
        }


}