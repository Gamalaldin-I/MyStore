package com.example.domain.repo

import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct

interface BillDetailsRepo{
    suspend fun getBillWithDetails(id:String): BillWithDetails
    suspend fun updateProductQuantityAfterReturn(id: String, quantity:Int):Pair<Boolean,String>
    suspend fun updateSaleCashAfterReturn(id: String, cash:Double):Pair<Boolean,String>
    suspend fun insertReturn(soldProduct: SoldProduct):Pair<Boolean,String>
    suspend fun updateBillProductQuantityAfterReturn(id: String, quantity:Int):Pair<Boolean,String>
    suspend fun deleteSoldProduct(soldProduct: SoldProduct):Pair<Boolean,String>
    suspend fun deleteBillById(id: String):Pair<Boolean,String>
    suspend fun isProductInStock(id: String): Boolean

}