package com.example.htopstore.util.firebase

import com.example.data.remote.RemoteSalesRepo
import com.example.domain.model.Bill
import com.example.domain.model.SoldProduct
import com.google.firebase.firestore.FirebaseFirestore

class RemoteSalesRepoImp( val db: FirebaseFirestore): RemoteSalesRepo {
    override fun addBill(bill: Bill) {
        TODO("Not yet implemented")
    }

    override fun deleteBillById(id: String) {
        TODO("Not yet implemented")
    }

    override fun getBills(): List<Bill> {
        TODO("Not yet implemented")
    }

    override fun addListOfBills(bills: List<Bill>) {
        TODO("Not yet implemented")
    }

    override fun addSoldProduct(soldProduct: SoldProduct) {
        TODO("Not yet implemented")
    }

    override fun deleteSoldProductById(id: String) {
        TODO("Not yet implemented")
    }

    override fun getSales(): List<SoldProduct> {
        TODO("Not yet implemented")
    }

    override fun addListOfSoldProducts(soldProducts: List<SoldProduct>) {
        TODO("Not yet implemented")
    }
}