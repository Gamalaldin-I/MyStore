package com.example.data.repo

import com.example.data.Mapper.toBillEntity
import com.example.data.Mapper.toSoldProduct
import com.example.data.Mapper.toSoldProductEntity
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.domain.model.Bill
import com.example.domain.model.SoldProduct
import com.example.domain.repo.SalesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SalesRepoImp(private val salesDao: SalesDao,private val productDao: ProductDao): SalesRepo {
    override suspend fun getReturns(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getReturns().map { it.toSoldProduct() }
        }


    override suspend fun insertBill(bill: Bill) {
        salesDao.insertBill(bill.toBillEntity())

    }

    override suspend fun insertBillDetails(soldProducts: List<SoldProduct>){
        val sold = soldProducts.map { it.toSoldProductEntity() }
        salesDao.insertBillDetails(sold)
    }

    override suspend fun updateQuantityAvailableAfterSell(
        productId: String, count: Int ) =
        productDao.updateProductQuantityAfterSale(productId, count)

    override suspend fun getAllSalesAndReturns(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getAllSalesAndReturns().map { it.toSoldProduct() }
        }

    override suspend fun getSoldOnly(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getSales().map { it.toSoldProduct() }
        }

    override  fun getReturnsByDate(date: String): Flow<List<SoldProduct>> =
            salesDao.getReturnsByDate(date).map { list->
                list.map { it.toSoldProduct() }
            }


    override suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getAllSalesAndReturnsByDate(date).map { it.toSoldProduct() }
        }

    override suspend fun getSoldOnlyByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getSalesByDate(date).map { it.toSoldProduct() }
        }



}