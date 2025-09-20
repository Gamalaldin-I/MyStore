package com.example.data.repo

import com.example.data.Mapper.toBill
import com.example.data.local.dao.SalesDao
import com.example.domain.model.Bill
import com.example.domain.repo.BillRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillRepoImp(private val salesDao: SalesDao): BillRepo {
    override suspend fun getBillsByDate(date: String): List<Bill>  =
        withContext(Dispatchers.IO) {
            salesDao.getBillsByDate(date).map { it.toBill() }
        }

    override suspend fun getAllBills(): List<Bill> =
        withContext(Dispatchers.IO) {
            salesDao.getAllBills().map { it.toBill() }
        }

    override suspend fun getBillsByDateRange(
        since: String,
        to: String,
    ): List<Bill> =
        withContext(Dispatchers.IO) {
            salesDao.getBillsByDateRange(since, to).map { it.toBill() }
        }

    override suspend fun getBillsTillDate(date: String): List<Bill> =
        withContext(Dispatchers.IO) {
            salesDao.getBillsTillDate(date).map { it.toBill() }
        }



}