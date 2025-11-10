package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Plan(
    val name: String,
    val description: String,
    val price: Double,
    val productsCount:Int,
    val operationsCount:Int
)