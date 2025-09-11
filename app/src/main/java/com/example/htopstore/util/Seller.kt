package com.example.htopstore.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.htopstore.data.local.SharedPref
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.data.local.repo.salesRepo.SalesRepoImp
import com.example.htopstore.domain.model.CartProduct
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Seller {
    lateinit var sharedPref : SharedPref
    lateinit var productRepo: ProductRepoImp
    lateinit var salesRepo : SalesRepoImp
    var currentOperationId : String = ""
    var currentDate : String = ""
    var currentTime: String = ""
    var totalCash : Double = 0.0
    val salesDetailsList = ArrayList<SoldProduct>()


    @OptIn(DelicateCoroutinesApi::class)
    fun sellAllItems(context: Context,cartList: List<CartProduct>,discount: Int = 0) {
        // sell all the items in the cart list
        productRepo = ProductRepoImp(context)
        salesRepo = SalesRepoImp(context)
        currentOperationId = IdGenerator.generateTimestampedId()
        currentDate = DateHelper.getCurrentDate()
        currentTime = DateHelper.getCurrentTime()
        Log.d("SELL_ERROR", "from sell all items ${cartList.size}")
        // sell all the items one by one
        for (item in cartList){
            sellItem(item,discount)
        }
        setCash(totalCash,context)
        val saleOp = SellOp(
            saleId = currentOperationId,
            date = currentDate,
            time = currentTime,
            totalCash = totalCash,
            discount = discount
        )
        GlobalScope.launch(Dispatchers.IO){
            salesRepo.insertSale(saleOp)
            salesRepo.insertSaleDetails(salesDetailsList)
            salesDetailsList.clear()
            currentOperationId = ""
            currentDate = ""
            currentTime = ""
        }
        totalCash = 0.0
        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

    }


    @OptIn(DelicateCoroutinesApi::class)
    fun sellItem(item: CartProduct,discount: Int = 0) {
        // get the item from the cart list and manage the count in the stock
        //save as an operation in the history
        // sell the item
        // update the cash
        val discountValue = (item.pricePerOne * discount)/100
        val priceAfterDisCount = item.pricePerOne - discountValue
        val soldProduct = SoldProduct(
            saleId = currentOperationId,
            detailId = IdGenerator.generateTimestampedId(),
            productId = item.id,
            name = item.name,
            type = item.type,
            quantity = item.sellingCount,
            price = item.buyingPrice,
            sellingPrice = priceAfterDisCount,
            sellDate = currentDate,
            sellTime = currentTime
            )
        totalCash+= (priceAfterDisCount * item.sellingCount)
        salesDetailsList.add(soldProduct)
        Log.d("SELL_ERROR", "from sell item ${soldProduct.name} ${soldProduct.quantity}")
        GlobalScope.launch(Dispatchers.IO){
            productRepo.updateProductQuantity(item.id, item.sellingCount)
        }

    }
    fun setCash(cash: Double, context: Context) {
        sharedPref = SharedPref(context)
        var m = sharedPref.getCash()
        m+=cash
        sharedPref.saveCash(m)

    }

}