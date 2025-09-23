package com.example.domain.repo

import com.example.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface AnalysisRepo {
    //for main dashboard
    fun getExpensesByDate(date: String): Flow<Double?>
    fun getTotalSalesOfToday(date: String): Flow<Double?>
    fun getProfitOfToday(date: String): Flow<Double?>
    //for product analysis
    fun getLowStock(): Flow<List<Product>>
    fun getTop5InSales(): Flow<List<Product>>
}