package com.example.htopstore.domain.useCase

import com.example.htopstore.data.local.model.Product
import com.example.htopstore.domain.model.CartProduct
import com.example.htopstore.util.CartHelper

class CartHandler(cartAddedList: ArrayList<Product>){
    private val cartList = ArrayList<CartProduct>()
    init {
        addAllToTheCartList(cartAddedList)
    }
    fun addToTheCartList(product: Product) {
        val cartProduct = CartProduct(
            id = product.id,
            name = product.name,
            type = product.category,
            image = product.productImage,
            buyingPrice = product.buyingPrice,
            pricePerOne = product.sellingPrice,
            maxLimitCount = product.count,
            sellingCount = 1
        )
        cartList.add(cartProduct)
    }
    fun addAllToTheCartList(list: ArrayList<Product>) {
        for (product in list){
            addToTheCartList(product)
    }
    }
    fun deleteFromTheCartList(product: CartProduct) {
        cartList.remove(product)
    }
    fun getTheTotalCartPrice(): Double{
        var totalPrice = 0.0
        for (product in cartList){
            totalPrice += (product.pricePerOne * product.sellingCount)
        }
        return totalPrice
    }
    fun getListOfCartProducts(): ArrayList<CartProduct>{
        return cartList
    }
    fun clearCartList(){
        cartList.clear()
        CartHelper.clearCartList()
    }


}