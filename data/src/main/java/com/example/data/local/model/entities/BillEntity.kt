package com.example.data.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sell_ops")
data class BillEntity(
    @PrimaryKey
    val saleId: String,
    val discount: Int,
    val date: String,
    val time: String,
    val totalCash: Double,
    var lastUpdate:String,
    val userId: String,
)