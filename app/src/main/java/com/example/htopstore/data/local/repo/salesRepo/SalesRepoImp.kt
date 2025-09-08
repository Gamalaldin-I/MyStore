package com.example.htopstore.data.local.repo.salesRepo

import android.content.Context
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalesRepoImp(context: Context): SalesRepo {
    val salesDao = AppDataBase.getDatabase(context).salesDao()
    override suspend fun insertSale(sale: SellOp) {
        withContext(Dispatchers.IO){
        salesDao.insertSaleOp(sale)}
    }

    override suspend fun insertSaleDetails(sales: List<SoldProduct>) {
        withContext(Dispatchers.IO){
        salesDao.insertSoldProducts(sales)}
    }

    override suspend fun insertSoldProduct(sold: SoldProduct) {
        withContext(Dispatchers.IO){
        salesDao.insertSoldProduct(sold)}
    }

    override suspend fun getReturns(): List<SoldProduct> =
        withContext(Dispatchers.IO){
        salesDao.getReturns()}

    override suspend fun getAllSalesAndReturns(): List<SoldProduct> =
        withContext(Dispatchers.IO) {
            salesDao.getAllSalesAndReturns()
        }

    override suspend fun getSoldOnly(): List<SoldProduct> =
        withContext(Dispatchers.IO){
        salesDao.getSales()}

    override suspend fun getReturnsByDate(date: String): List<SoldProduct> =
        withContext (Dispatchers.IO){
            salesDao.getReturnsByDate(date)
        }


    override suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO){
        salesDao.getAllSalesAndReturnsByDate(date)}

    override suspend fun getSoldOnlyByDate(date: String): List<SoldProduct> =
        withContext(Dispatchers.IO){
        salesDao.getSalesByDate(date)}

    override suspend fun getAllSalesWithDetails(): List<SalesOpsWithDetails> =
         withContext(Dispatchers.IO){
        salesDao.getAllSalesOpsWithDetails()}


    override suspend fun getSaleWithDetails(saleId: String): SalesOpsWithDetails? =
        withContext(Dispatchers.IO){
        salesDao.getSalesOpWithDetails(saleId)}

    override suspend fun getAllSellOp(): List<SellOp> =
        withContext(Dispatchers.IO){
        salesDao.getAllSalesOps()}

    override suspend fun deleteSaleById(saleId: String) {
        withContext(Dispatchers.IO){
        salesDao.deleteSaleById(saleId)}

    }

    override suspend fun updateSoldProduct(soldProduct: SoldProduct) {
        withContext(Dispatchers.IO){
        salesDao.updateSoldProduct(soldProduct)}
    }

    override suspend fun deleteSoldProduct(soldProduct: SoldProduct) {
        withContext(Dispatchers.IO){
        salesDao.deleteSoldProduct(soldProduct)}

    }

    override suspend fun updateSaleCashAfterReturn(id: String, returnCash: Double) {
        withContext(Dispatchers.IO){
            salesDao.updateSaleCashAfterReturn(id, returnCash)}
        }

    override suspend fun getSalesByDate(date: String): List<SellOp> {
        return withContext(Dispatchers.IO){
            salesDao.getAllSalesOpsByDate(date)}

    }
}