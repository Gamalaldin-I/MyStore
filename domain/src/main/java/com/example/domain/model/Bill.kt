package com.example.domain.model

data class Bill(
    val saleId: String,
    val discount: Int,
    val date: String,
    val time: String,
    val totalCash: Double,
    var lastUpdate: String
)