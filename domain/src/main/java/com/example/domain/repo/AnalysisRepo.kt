package com.example.domain.repo

import com.example.domain.model.CategorySales
import com.example.domain.model.ExpensesWithCategory
import com.example.domain.model.Product
import com.example.domain.model.SalesProfitByPeriod
import kotlinx.coroutines.flow.Flow

interface AnalysisRepo {
    //for main dashboard
    fun getExpensesByDate(date: String): Flow<Double?>
    fun getTotalSalesOfToday(date: String): Flow<Double?>
    fun getProfitOfToday(date: String): Flow<Double?>
    //for product analysis
    fun getLowStock(): Flow<List<Product>>
    fun getTop10InSales(): Flow<List<Product>>



    //for analysis activity
    //for product analysis
    suspend fun getTheProductsWithHighestProfit():List<Product>
    suspend fun getProductsThatHaveNotBeenSold():List<Product>
    suspend fun getTheMostSellingCategory(startDate: String, endDate: String):String
    suspend fun getTheLeastSellingCategory(startDate: String, endDate: String):String
    suspend fun getSellingCategoriesByDate(startDate: String, endDate: String): List<CategorySales>
    suspend fun getReturningCategoriesByDate(startDate: String, endDate: String): List<CategorySales>
    suspend fun getTheDaysOfSales():List<String>
    suspend fun getSpecificDay(day:String):String
    //for day analysis
    suspend fun getTheHoursWithHighestSales(period:String):String?
    suspend fun getTheDaysWithHighestSales(period:String):String?
    suspend fun getTheAvgOfSales(period:String):Double?
    suspend fun getNumberOfSales(period: String):Int?
    suspend fun getTheProfit(period:String):Double?
    suspend fun getTheTotalSales(period:String):Double?
    suspend fun getTheSalesAndProfitGroupedByPeriod(period:String):List<SalesProfitByPeriod>?
    //for expenses
    suspend fun getExpensesListByPeriod(date:String,endDate: String):List<ExpensesWithCategory>?




}