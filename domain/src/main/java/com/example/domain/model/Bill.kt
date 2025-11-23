package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: String,
    val discount: Int,
    val date: String,
    val time: String,
    val totalCash: Double,
    var lastUpdate: String,
    val storeId:String,
    val userId:String,
    var deleted:Boolean
)