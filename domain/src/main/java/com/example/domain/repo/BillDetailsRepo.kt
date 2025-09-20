package com.example.domain.repo

import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct

interface BillDetailsRepo{
    suspend fun getBillWithDetails(id:String): BillWithDetails
    suspend fun updateProductQuantityAfterReturn(id: String, quantity:Int)
    suspend fun updateSaleCashAfterReturn(id: String, cash:Double)
    suspend fun insertReturn(soldProduct: SoldProduct)
    suspend fun updateBillProductQuantityAfterReturn(id: String, quantity:Int)
    suspend fun deleteSoldProduct(soldProduct: SoldProduct)
    suspend fun deleteSaleById(id: String)

}