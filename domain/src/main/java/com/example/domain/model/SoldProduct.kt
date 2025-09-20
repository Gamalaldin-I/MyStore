package com.example.domain.model

data class SoldProduct(
    val detailId: String,
    val saleId: String?,
    val productId: String?,
    val name: String,
    val type: String,
    var quantity: Int,
    val price: Double,
    val sellingPrice: Double,
    val sellDate: String,
    val sellTime: String
)