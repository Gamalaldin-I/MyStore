package com.example.data.repo

import com.example.data.Mapper.toBillWithDetails
import com.example.data.Mapper.toExpenseEntity
import com.example.data.Mapper.toSoldProductEntity
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.domain.model.BillWithDetails
import com.example.domain.model.Expense
import com.example.domain.model.SoldProduct
import com.example.domain.repo.BillDetailsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillDetailsRepoImp
    (private val salesDao: SalesDao, private val productDao: ProductDao,private val expenseDao: ExpenseDao): BillDetailsRepo {

    override suspend fun getBillWithDetails(id: String): BillWithDetails =
        withContext(Dispatchers.IO) {
            salesDao.getBillWithDetails(id).toBillWithDetails()
        }

    override suspend fun updateProductQuantityAfterReturn(idOfProduct: String, quantity: Int) {
        productDao.updateProductQuantityAfterReturn(idOfProduct, quantity)
    }

    override suspend fun updateSaleCashAfterReturn(id: String, cash: Double) {
        withContext(Dispatchers.IO) {
            salesDao.updateSaleCashAfterReturn(id, cash)
        }
    }

    override suspend fun insertReturn(soldProduct: SoldProduct) {
        withContext(Dispatchers.IO) {
            salesDao.insertSoldProduct(soldProduct.toSoldProductEntity())
        }
    }


    override suspend fun updateBillProductQuantityAfterReturn(id: String,quantity: Int) {
        withContext(Dispatchers.IO) {
            salesDao.updateBillProductQuantityAfterReturn(id, quantity)
        }
    }

    override suspend fun deleteSoldProduct(soldProduct: SoldProduct) {
        withContext(Dispatchers.IO) {
            salesDao.deleteSoldProduct(soldProduct.toSoldProductEntity())
        }
    }

    override suspend fun deleteSaleById(id: String) {
        withContext(Dispatchers.IO) {
            salesDao.deleteSaleById(id)
        }
    }

    override suspend fun isProductInStock(id: String): Boolean {
        return (((productDao.isProductInStock(id) ?: 0) > 0))
    }
    override suspend fun insertExpense(expense: Expense) {
            expenseDao.insertExpense(expense.toExpenseEntity())
    }

}