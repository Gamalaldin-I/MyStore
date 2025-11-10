package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStoreRequest(
    val name: String,
    val phone: String,
    val location: String,
    val planProductLimit: Int,
    val planOperationLimit: Int,
    val plan: String,
    val logoUrl: String
)