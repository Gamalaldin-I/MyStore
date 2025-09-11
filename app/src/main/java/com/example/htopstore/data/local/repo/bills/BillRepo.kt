package com.example.htopstore.data.local.repo.bills

import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails

interface BillRepo {
    //for sales details for bills Activity
    suspend fun getBillsByDate(date: String): List<SellOp>
    suspend fun getAllBills(): List<SellOp>
    suspend fun getBillsByDateRange(since: String, to:String): List<SellOp>
    suspend fun getBillsTillDate(date: String): List<SellOp>
}