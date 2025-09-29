package com.example.data.repo

import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.local.model.entities.ProductEntity
import com.example.domain.model.CategorySales
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
        expenseDao.getTotalExpensesForDate(date)


    override fun getTotalSalesOfToday(date: String): Flow<Double?> =
        salesDao.getTotalSalesOfToday(date)


    override fun getProfitOfToday(date: String): Flow<Double?> =
        salesDao.getProfitOfToday(date)


    override  fun getLowStock(): Flow<List<Product>> =
        productDao.getLowStock().mapData()

    override  fun getTop10InSales(): Flow<List<Product>> =
        productDao.getTop10InSales().mapData()

    override suspend fun getTheDaysOfSales(): List<String> =
        salesDao.getWorkDays()

    override suspend fun getSpecificDay(day: String): String =
        salesDao.getSpecificDay(day)

    override suspend fun getTheProductsWithHighestProfit(): List<Product> =
        productDao.getProductsByHighestProfit().mapData()


    override suspend fun getProductsThatHaveNotBeenSold(): List<Product> =
        productDao.getProductsThatHaveNotBeenSold().mapData()

    override suspend fun getTheMostSellingCategory(startDate: String, endDate: String): String =
        salesDao.getTheMostSellingCategoryByDate(startDate, endDate)

    override suspend fun getTheLeastSellingCategory(startDate: String, endDate: String): String =
        salesDao.getTheLeastSellingCategoryByDate(startDate, endDate)

    override suspend fun getSellingCategoriesByDate(
        startDate: String,
        endDate: String,
    ): List<CategorySales> =
        salesDao.getSellingCategoriesByDate(startDate, endDate)

    override suspend fun getReturningCategoriesByDate(
        startDate: String,
        endDate: String,
    ): List<CategorySales> =
        salesDao.getReturningCategoriesByDate(startDate, endDate)

    override suspend fun getTheHoursWithHighestSales(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getTheDaysWithHighestSales(): List<String> {
        TODO("Not yet implemented")
    }


    fun Flow<List<ProductEntity>>.mapData():Flow<List<Product>> {
        return this.map {
            it.map {
                it.toDomain()
            }
        }
    }
    fun List<ProductEntity>.mapData():List<Product> {
        return this.map {
            it.toDomain()
        }
    }
}