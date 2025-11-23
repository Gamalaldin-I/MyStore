package com.example.data.repo

import com.example.data.Mapper.toBillWithDetails
import com.example.data.Mapper.toSoldProductEntity
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.remote.repo.RemoteBillRepo
import com.example.data.remote.repo.RemoteSalesRepo
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.domain.repo.BillDetailsRepo

class BillDetailsRepoImp(
    private val salesDao: SalesDao,
     private val productDao: ProductDao,
     private val remoteBills: RemoteBillRepo,
     private val remoteSales: RemoteSalesRepo): BillDetailsRepo {

    override suspend fun getBillWithDetails(id: String): BillWithDetails =
        salesDao.getBillWithDetails(id).toBillWithDetails()


    override suspend fun updateProductQuantityAfterReturn(idOfProduct: String, quantity: Int) =
        remoteSales.updateQuantityAvailable(idOfProduct, quantity, isSell = false) {
        productDao.updateProductQuantityAfterReturn(idOfProduct, quantity)
        }


    override suspend fun updateSaleCashAfterReturn(id: String, cash: Double) =
        remoteBills.updateBillCashAfterReturn(id, cash) {
            salesDao.updateSaleCashAfterReturn(id, cash)
        }


    override suspend fun insertReturn(soldProduct: SoldProduct) =
        remoteSales.addSales(listOf(soldProduct)){
            salesDao.insertSoldProduct(soldProduct.toSoldProductEntity())
        }



    override suspend fun updateBillProductQuantityAfterReturn(id: String,quantity: Int) =
        remoteSales.updateSoldProductAfterReturn(id, quantity) {
        salesDao.updateBillProductQuantityAfterReturn(id, quantity)
        }


    override suspend fun deleteSoldProduct(soldProduct: SoldProduct) =
        remoteSales.deleteSoldProduct(soldProduct.id){
            salesDao.deleteSoldProduct(soldProduct.toSoldProductEntity())
        }


    override suspend fun deleteBillById(id: String) =
        remoteBills.deleteBill(id) {
            salesDao.deleteSaleById(id)
        }


    override suspend fun isProductInStock(id: String): Boolean {
        return (((productDao.isProductInStock(id) ?: 0) > 0))
    }


}