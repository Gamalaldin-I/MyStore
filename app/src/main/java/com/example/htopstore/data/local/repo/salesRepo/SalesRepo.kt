package com.example.htopstore.data.local.repo.salesRepo

import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails

interface SalesRepo {


    //for sell operations
    suspend fun insertSale(sale: SellOp)
    suspend fun insertSaleDetails(sales: List<SoldProduct>)
    suspend fun insertSoldProduct(soldProduct: SoldProduct)


    //for sales Activity
    suspend fun getReturns(): List<SoldProduct>
    suspend fun getAllSalesAndReturns(): List<SoldProduct>
    suspend fun getSoldOnly(): List<SoldProduct>
    //filter by date
    suspend fun getReturnsByDate(date: String): List<SoldProduct>
    suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct>
    suspend fun getSoldOnlyByDate(date: String): List<SoldProduct>

    //for sales details for bills Activity
    suspend fun getAllSalesWithDetails(): List<SalesOpsWithDetails>
    suspend fun getSalesByDate(date: String): List<SellOp>
    suspend fun getSaleWithDetails(saleId: String): SalesOpsWithDetails?
    suspend fun getAllSellOp(): List<SellOp>

    // manage sales
    suspend fun deleteSaleById(saleId: String)

    //update sold product
    suspend fun updateSoldProduct(soldProduct: SoldProduct)
    suspend fun deleteSoldProduct(soldProduct: SoldProduct)
    suspend fun updateSaleCashAfterReturn(id: String, returnCash: Double)


}