package com.example.domain.model

data class Product(
    val id:String,
    val productImage:String,
    val addingDate:String,
    var category:String,
    var name:String,
    var count:Int,
    var soldCount:Int,
    var buyingPrice:Double,
    var sellingPrice:Double)
