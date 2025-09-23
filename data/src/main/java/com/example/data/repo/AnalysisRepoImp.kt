package com.example.data.repo

import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.local.model.entities.ProductEntity
import com.example.domain.model.Product
import com.example.domain.repo.AnalysisRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnalysisRepoImp(
    private val salesDao: SalesDao,
    private val expenseDao: ExpenseDao,
    private val productDao: ProductDao
) : AnalysisRepo {
    override fun getExpensesByDate(date: String): Flow<Double?> =
        expenseDao.getExpensesByDate(date)


    override fun getTotalSalesOfToday(date: String): Flow<Double?> =
        salesDao.getTotalSalesOfToday(date)


    override fun getProfitOfToday(date: String): Flow<Double?> =
        salesDao.getProfitOfToday(date)


    override  fun getLowStock(): Flow<List<Product>> =
        productDao.getLowStock().mapData()

    override  fun getTop5InSales(): Flow<List<Product>> =
        productDao.getTop5InSales().mapData()


    fun Flow<List<ProductEntity>>.mapData():Flow<List<Product>> {
        return this.map {
            it.map {
                it.toDomain()
            }
        }
    }
}