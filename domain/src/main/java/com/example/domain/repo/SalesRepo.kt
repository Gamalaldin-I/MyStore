package com.example.domain.repo

import com.example.domain.model.PendingSellAction
import com.example.domain.model.SoldProduct
import kotlinx.coroutines.flow.Flow

interface SalesRepo{

    //for sell operation
    suspend fun insertBillDetails(soldProducts: List<SoldProduct>):Pair<Boolean,String>
    suspend fun updateQuantityAvailableAfterSell(productId: String, count:Int):Pair<Boolean,String>
    suspend fun fetchSalesFromRemote():Pair<Boolean,String>
    suspend fun insertPendingSellAction(pending: PendingSellAction)
    suspend fun updatePendingSellAction(pending: PendingSellAction)
    fun getAllPendingAndApproved(): Flow<List<PendingSellAction>>
    suspend fun getPendingActionById(id:Int): PendingSellAction
    suspend fun deleteApprovedSellAction()
    suspend fun deletePendingActionById(id: Int)



    //for sales and returns views
    suspend fun getAllSalesAndReturns(): List<SoldProduct>
    suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct>
    suspend fun getSoldOnly(): List<SoldProduct>
    suspend fun getSoldOnlyByDate(date: String): List<SoldProduct>
    suspend fun getReturns(): List<SoldProduct>
     fun getReturnsByDate(date: String): Flow<List<SoldProduct>>
    suspend fun getTotalOfSalesByDateRange(start: String, end:String): Double?
    suspend fun getTotalOfProfitByDateRange(start: String, end:String): Double?


}