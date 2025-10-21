package com.example.data.remote

import com.example.domain.model.Bill
import com.example.domain.model.SoldProduct

interface RemoteSalesRepo {
    fun addBill(bill: Bill)
    fun deleteBillById(id: String)
    fun getBills(): List<Bill>
    fun addListOfBills(bills: List<Bill>)


    fun addSoldProduct(soldProduct: SoldProduct)
    fun deleteSoldProductById(id: String)
    fun getSales(): List<SoldProduct>
    fun addListOfSoldProducts(soldProducts: List<SoldProduct>)
}