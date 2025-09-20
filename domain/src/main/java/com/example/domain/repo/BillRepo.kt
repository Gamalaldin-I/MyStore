package com.example.domain.repo

import com.example.domain.model.Bill

interface BillRepo {
    //for sales details for bills Activity
    suspend fun getBillsByDate(date: String): List<Bill>
    suspend fun getAllBills(): List<Bill>
    suspend fun getBillsByDateRange(since: String, to:String): List<Bill>
    suspend fun getBillsTillDate(date: String): List<Bill>
}