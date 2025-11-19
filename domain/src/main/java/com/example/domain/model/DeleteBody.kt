package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeleteBody(
    val lastUpdate:String,
    val deleted: Boolean
)
