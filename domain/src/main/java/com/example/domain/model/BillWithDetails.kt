package com.example.domain.model

data class BillWithDetails(
    val bill: Bill,
    val soldProducts: List<SoldProduct>
)