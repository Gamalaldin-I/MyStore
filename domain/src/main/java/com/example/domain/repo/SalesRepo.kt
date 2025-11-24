package com.example.domain.repo

import com.example.domain.model.CartProduct
import com.example.domain.model.SoldProduct
import kotlinx.coroutines.flow.Flow

interface SalesRepo{

    //for sell operation
    suspend fun insertBillDetails(soldProducts: List<SoldProduct>):Pair<Boolean,String>
    suspend fun updateQuantityAvailableAfterSell(productId: String, count:Int):Pair<Boolean,String>
    suspend fun fetchSalesFromRemote():Pair<Boolean,String>
    suspend fun insertPendingSellAction(cartList: List<CartProduct>,
                                        discount:Int,
                                        billInserted:Boolean,
                                        progress:Int,
                                        soldItemsInserted: Boolean)
    suspend fun updatePendingSellAction(
        id: Int,
        status: String,
        soldProducts: List<CartProduct>,
        progress:Int,
        discount:Int,
        billInserted:Boolean,
        soldItemsInserted: Boolean
    )
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