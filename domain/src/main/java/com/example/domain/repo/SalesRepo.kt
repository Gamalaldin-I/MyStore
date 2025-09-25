package com.example.domain.repo

import com.example.domain.model.Bill
import com.example.domain.model.SoldProduct
import kotlinx.coroutines.flow.Flow

interface SalesRepo{

    //for sell operation
    suspend fun insertBill(bill: Bill)
    suspend fun insertBillDetails(soldProducts: List<SoldProduct>)
    suspend fun updateQuantityAvailableAfterSell(productId: String, count:Int)

    //for sales and returns views
    suspend fun getAllSalesAndReturns(): List<SoldProduct>
    suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct>
    suspend fun getSoldOnly(): List<SoldProduct>
    suspend fun getSoldOnlyByDate(date: String): List<SoldProduct>
    suspend fun getReturns(): List<SoldProduct>
     fun getReturnsByDate(date: String): Flow<List<SoldProduct>>
}