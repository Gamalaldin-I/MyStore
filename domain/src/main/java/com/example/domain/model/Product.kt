package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id:String,
    var productImage:String,
    val addingDate:String,
    var category:String,
    var name:String,
    var count:Int,
    var soldCount:Int,
    var buyingPrice:Double,
    var sellingPrice:Double,
    var lastUpdate: String,
    var storeId: String?
)
