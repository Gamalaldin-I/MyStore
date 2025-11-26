package com.example.data.repo

import android.util.Log
import com.example.data.Mapper.toPendingSellAction
import com.example.data.Mapper.toPendingSellActionEntity
import com.example.data.Mapper.toSoldProduct
import com.example.data.Mapper.toSoldProductEntity
import com.example.data.local.dao.PendingSellDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.remote.repo.RemoteSalesRepo
import com.example.domain.model.PendingSellAction
import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SalesRepoImp(private val salesDao: SalesDao,
                   private val productDao: ProductDao,
                   private val remote: RemoteSalesRepo,
                   private val pendingSellDao:PendingSellDao
): SalesRepo {

    override suspend fun getReturns(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getReturns().map { it.toSoldProduct() }
        }




    override suspend fun insertBillDetails(soldProducts: List<SoldProduct>)=
        remote.addSales(soldProducts){
        val sales = soldProducts.map { it.toSoldProductEntity() }
            Log.d("DEBUGRESR","${sales[0]}")
        salesDao.insertBillDetails(sales)
        }


    override suspend fun updateQuantityAvailableAfterSell(
        productId: String, count: Int ) =
        remote.updateQuantityAvailable(productId,count,true){
            productDao.updateProductQuantityAfterSale(productId, count)
        }

    override suspend fun fetchSalesFromRemote(): Pair<Boolean, String> {
        return remote.fetchSales { sales->
            sales.forEach { soldProduct ->
                Log.d("FETCH_PROCESS","soldProduct  $soldProduct")
                if(soldProduct.deleted){
                    salesDao.deleteSaleById(soldProduct.id)
                }else{
                    salesDao.insertSoldProduct(soldProduct.toSoldProductEntity())
                    Log.d("FETCH_PROCESS","inserted  $soldProduct")

                }

            }
        }

    }

    override suspend fun insertPendingSellAction(
        pendingSellAction: PendingSellAction
    ) {
        pendingSellDao.insertPendingSellAction(pendingSellAction.toPendingSellActionEntity())
    }

    override suspend fun updatePendingSellAction(
        updatedSellPendingAction: PendingSellAction
    ){
        pendingSellDao.updatePendingSellAction(updatedSellPendingAction.toPendingSellActionEntity())
    }

    override fun getAllPendingAndApproved(): Flow<List<PendingSellAction>> {
        return pendingSellDao.getAllPendingSellActions().map { list->
            list.map {
                it.toPendingSellAction()
            }
            }
    }

    override suspend fun getPendingActionById(id: Int): PendingSellAction {
        return pendingSellDao.getPendingSellActionById(id).toPendingSellAction()
    }

    override suspend fun deleteApprovedSellAction() {
        pendingSellDao.deleteAllApprovedActions()
    }

    override suspend fun deletePendingActionById(id: Int) {
        pendingSellDao.deletePendingSellAction(id)
    }


    override suspend fun getAllSalesAndReturns(): List<SoldProduct> =
            salesDao.getAllSalesAndReturns().map { it.toSoldProduct() }


    override suspend fun getSoldOnly(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getSales().map { it.toSoldProduct() }
        }

    override  fun getReturnsByDate(date: String): Flow<List<SoldProduct>> =
            salesDao.getReturnsByDate(date).map { list->
                list.map { it.toSoldProduct() }
            }

    override suspend fun getTotalOfSalesByDateRange(
        start: String,
        end: String,
    ): Double? = salesDao.getTheTotalOfSalesByDateRange(start, end)

    override suspend fun getTotalOfProfitByDateRange(
        start: String,
        end: String,
    ): Double? = salesDao.getTheTotalOfProfitByRange(start, end)


    override suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getAllSalesAndReturnsByDate(date).map { it.toSoldProduct() }
        }

    override suspend fun getSoldOnlyByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getSalesByDate(date).map { it.toSoldProduct() }
        }



}