package com.example.htopstore.util

import com.example.htopstore.data.local.model.Product

object CartHelper {
    private val cartList = ArrayList<Product>()
    fun addToTheCartList(product: Product) {
        if ( product in cartList){
            return
        }
        else cartList.add(product)
    }
    fun removeFromTheCartList(productId: String ) {
        if(cartList.isEmpty()){
            return
        }
        else{
            for (product in cartList){
                if (product.id == productId){
                    cartList.remove(product)
                    break
                }
            }
        }
    }
    fun getAddedTOCartProducts(): ArrayList<Product>{
        return cartList
    }
    fun clearCartList(){
        cartList.clear()

    }
}