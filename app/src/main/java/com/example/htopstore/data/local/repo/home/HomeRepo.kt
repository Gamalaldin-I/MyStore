package com.example.htopstore.data.local.repo.home

import com.example.htopstore.data.local.model.Expense
import com.example.htopstore.data.local.model.Product

interface HomeRepo {
    suspend fun getTop5InSales(): List<Product>
    suspend fun getLowStock(): List<Product>
    //today short report
    suspend fun getExpensesToday(date:String): Double?
    suspend fun getIncomeToday(date: String): Double?
    suspend fun getProfitToday(date: String): Double?



}