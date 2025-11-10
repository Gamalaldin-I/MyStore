package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    var id: String,
    var name: String,
    var location: String,
    var phone: String,
    val ownerId:String,
    var plan: String,
    var planProductLimit: Int,
    var planOperationLimit: Int,
    var productsCount:Int,
    var operationsCount: Int,
    var resetDate: String,
    var logoUrl: String
)

