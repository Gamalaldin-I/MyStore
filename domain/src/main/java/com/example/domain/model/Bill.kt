package com.example.domain.model

data class Bill(
    val id: String,
    val discount: Int,
    val date: String,
    val time: String,
    val totalCash: Double,
    var lastUpdate: String,
    val storeId:String
)