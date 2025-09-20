package com.example.domain.model

data class CartProduct(
    val id: String,
    val name: String,
    val type: String,
    val image: String,
    val maxLimitCount:Int,
    val buyingPrice: Double,
    val pricePerOne: Double,
    var sellingCount: Int = 1
)