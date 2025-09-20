package com.example.data.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String,
    val date: String,
    val time: String,
    val description: String,
    val category: String,
    val amount: Double,
    val paymentMethod: String // bank or cash
)