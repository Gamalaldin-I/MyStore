package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val date: String,
    val time: String,
    val description: String,
    val category: String,
    val amount: Double,
    val paymentMethod: String,
    var lastUpdate: String,
    val storeId:String,
    val userId:String,
    var deleted:Boolean,
)
