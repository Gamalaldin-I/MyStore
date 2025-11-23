package com.example.domain.model.remoteModels

import kotlinx.serialization.Serializable

@Serializable
data class ProductQuantityUpdate(
    val count: Int,
    val soldCount: Int,
    val lastUpdate: String
)