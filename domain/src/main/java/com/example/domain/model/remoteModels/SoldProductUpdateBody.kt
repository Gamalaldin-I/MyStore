package com.example.domain.model.remoteModels

import kotlinx.serialization.Serializable

@Serializable
data class SoldProductUpdateBody(
    val quantity: Int,
    val lastUpdate: String
)
