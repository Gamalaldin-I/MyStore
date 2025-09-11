package com.example.htopstore.data.local.repo.bills

import android.content.Context
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillRepoImp(context: Context): BillRepo {
    private val db = AppDataBase.getDatabase(context)
    private val salesDao = db.salesDao()
    override suspend fun getBillsByDate(date: String): List<SellOp>  =
        withContext(Dispatchers.IO) {
            salesDao.getBillsByDate(date)
        }

    override suspend fun getAllBills(): List<SellOp> =
        withContext(Dispatchers.IO) {
            salesDao.getAllBills()
        }

    override suspend fun getBillsByDateRange(
        since: String,
        to: String,
    ): List<SellOp> =
        withContext(Dispatchers.IO) {
            salesDao.getBillsByDateRange(since, to)
        }

    override suspend fun getBillsTillDate(date: String): List<SellOp> =
        withContext(Dispatchers.IO) {
            salesDao.getBillsTillDate(date)
        }



}