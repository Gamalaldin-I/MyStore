package com.example.htopstore.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "sell_ops")
data class SellOp(
    @PrimaryKey
    val saleId: String,
    val discount: Int,
    val date: String,
    val time: String,
    val totalCash: Double
)
