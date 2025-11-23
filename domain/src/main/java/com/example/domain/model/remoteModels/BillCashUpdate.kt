package com.example.domain.model.remoteModels

import kotlinx.serialization.Serializable

@Serializable
data class BillCashUpdate(
    val userId:String,
    val totalCash: Double,
    val lastUpdate: String
)