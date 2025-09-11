package com.example.htopstore.data.local.repo.billDetails

import android.content.Context
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillDetailsRepoImp
    @Inject constructor(context: Context): BillDetailsRepo{
    private val billSaleDao = AppDataBase.getDatabase(context).salesDao()
    private val productDao = AppDataBase.getDatabase(context).productDao()

    override suspend fun getBillWithDetails(id: String): SalesOpsWithDetails =
        withContext(Dispatchers.IO){
            billSaleDao.getBillWithDetails(id)
        }

    override suspend fun updateProductQuantityAfterReturn(idOfProduct: String, quantity: Int) {
        withContext(Dispatchers.IO) {
            productDao.updateProductQuantityAfterReturn(idOfProduct, quantity)
        }
    }

    override suspend fun updateSaleCashAfterReturn(id: String, cash: Double) {
        withContext(Dispatchers.IO){
            billSaleDao.updateSaleCashAfterReturn(id,cash)
        }
    }

    override suspend fun insertReturn(soldProduct: SoldProduct) {
        withContext (Dispatchers.IO){
            billSaleDao.insertSoldProduct(soldProduct)
        }
    }


    override suspend fun updateBillProductQuantityAfterReturn(id: String,quantity: Int) {
        withContext(Dispatchers.IO) {
            billSaleDao.updateBillProductQuantityAfterReturn(id, quantity)
        }
    }

    override suspend fun deleteSoldProduct(soldProduct: SoldProduct) {
        withContext(Dispatchers.IO) {
            billSaleDao.deleteSoldProduct(soldProduct)
        }
    }

    override suspend fun deleteSaleById(id: String) {
        withContext(Dispatchers.IO) {
            billSaleDao.deleteSaleById(id)
        }
    }

}