package com.example.domain.model

data class Expense(
    val expenseId: String,
    val date: String,
    val time: String,
    val description: String,
    val category: String,
    val amount: Double,
    val paymentMethod: String // bank or cash
)
