package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SoldProduct(
    val id: String,
    val sellDate: String,
    val sellTime: String,
    val billId: String?,
    val productId: String?,
    val name: String,
    val type: String,
    var quantity: Int,
    val price: Double,
    val sellingPrice: Double,
    var lastUpdate: String,
    var deleted: Boolean,
    var storeId:String,
    var userId:String
    )