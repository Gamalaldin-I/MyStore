package com.example.htopstore.data.local.repo.billDetails

import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails

interface BillDetailsRepo{
    suspend fun getBillWithDetails(id:String): SalesOpsWithDetails
    suspend fun updateProductQuantityAfterReturn(id: String, quantity:Int)
    suspend fun updateSaleCashAfterReturn(id: String, cash:Double)
    suspend fun insertReturn(soldProduct: SoldProduct)
    suspend fun updateBillProductQuantityAfterReturn(id: String, quantity:Int)
    suspend fun deleteSoldProduct(soldProduct: SoldProduct)
    suspend fun deleteSaleById(id: String)

}