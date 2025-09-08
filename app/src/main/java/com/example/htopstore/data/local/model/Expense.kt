package com.example.htopstore.data.local.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val expenseId: String,
    val date: String,
    val time: String,
    val description: String, // وصف المصروف (إيجار، كهربا، مرتبات، إلخ)
    val category: String, // managirail
    val amount: Double,
    val paymentMethod: String // bank or cash
)

