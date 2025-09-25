package com.example.domain.repo

import com.example.domain.model.Bill
import kotlinx.coroutines.flow.Flow

interface BillRepo {
    //for sales details for bills Activity
     fun getBillsByDate(date: String): Flow<List<Bill>>
    suspend fun getAllBills(): List<Bill>
    suspend fun getBillsByDateRange(since: String, to:String): List<Bill>
    suspend fun getBillsTillDate(date: String): List<Bill>
}