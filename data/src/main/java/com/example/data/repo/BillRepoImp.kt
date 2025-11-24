package com.example.data.repo

import android.util.Log
import com.example.data.Mapper.toBill
import com.example.data.Mapper.toBillEntity
import com.example.data.local.dao.SalesDao
import com.example.data.remote.repo.RemoteBillRepo
import com.example.domain.model.Bill
import com.example.domain.repo.BillRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BillRepoImp(
    private val salesDao: SalesDao,
    private val remote: RemoteBillRepo
    ): BillRepo {

    override suspend fun insertBill(bill: Bill): Pair<Boolean, String> {
        return remote.addBill(bill){
            salesDao.insertBill(bill.toBillEntity())
        }

    }
    override suspend fun fetchBills(): Pair<Boolean, String> {
        return remote.fetchBills { bills ->
            bills.forEach { bill ->
                Log.d("FETCH_PROCESS","bill  $bill")
                if(bill.deleted){
                    salesDao.deleteSaleById(bill.id)
                }else{
                    salesDao.insertBill(bill.toBillEntity())

                }
            }
        }
    }

    override suspend fun isBillFoundInDB(id: String): Boolean =
        remote.isTheBillFound(id)

    override  fun getBillsByDate(date: String): Flow<List<Bill>> =
        salesDao.getBillsByDate(date).map{ list ->
            list.map { it.toBill() }
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