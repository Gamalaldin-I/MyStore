package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    var id: String,
    var name: String,
    var location: String,
    var phone: String,
    val ownerId:String
)

