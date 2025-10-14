package com.example.data.repo

import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.local.model.entities.ProductEntity
import com.example.domain.model.CategorySales
import com.example.domain.model.ExpensesWithCategory
import com.example.domain.model.Product
import com.example.domain.model.SalesProfitByPeriod
import com.example.domain.repo.AnalysisRepo
import com.example.domain.util.DateHelper
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

    override suspend fun getTheHoursWithHighestSales(period: String): String? {
        val (startDate,endDate) = DateHelper.giveStartAndEndDateFromToDay(duration = period)
        return salesDao.getTheBestPeriodOfDaySelling(startDate, endDate)?.period ?: ""
    }

    override suspend fun getTheDaysWithHighestSales(period: String): String? {
        val (startDate,endDate) = DateHelper.giveStartAndEndDateFromToDay(duration = period)
        return salesDao.getTheBestWeekDayOfSelling(startDate, endDate)?.dayOfWeek ?: ""
    }

    override suspend fun getTheAvgOfSales(period: String): Double? {
        val (s,e) = DateHelper.giveStartAndEndDateFromToDay(period)
        return salesDao.getAvg(s,e)
    }

    override suspend fun getNumberOfSales(period: String): Int ?{
        val (s,e) = DateHelper.giveStartAndEndDateFromToDay(period)
       return salesDao.getNumberOfSales(s,e)
    }


    override suspend fun getTheProfit(period: String): Double?{
        val (s,e) = DateHelper.giveStartAndEndDateFromToDay(period)
        return salesDao.getProfit(s,e)
    }

    override suspend fun getTheTotalSales(period: String): Double? {
        val (s,e) = DateHelper.giveStartAndEndDateFromToDay(period)
        return salesDao.getTotalSalesValue(s,e)
    }

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


    override suspend fun getTheSalesAndProfitGroupedByPeriod(period: String): List<SalesProfitByPeriod>? {
        val (startDate, endDate) = DateHelper.giveStartAndEndDateFromToDay(period)
        return when (period) {
            "Day" ->  salesDao.getSalesAndProfitGroupedByDay(startDate)
            else ->  salesDao.getSalesAndProfitGroupedByPeriod(startDate,endDate)
        }
    }

    override suspend fun getExpensesListByPeriod(
        startDate: String,
        endDate: String,
    ): List<ExpensesWithCategory> {
       return expenseDao.getTheExpensesByCategories(startDate,endDate)
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