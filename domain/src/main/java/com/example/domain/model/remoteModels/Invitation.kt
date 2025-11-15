package com.example.domain.model.remoteModels

import kotlinx.serialization.Serializable

@Serializable
data class Invitation(
    val id:String?=null,
    var acceptedAt: String? = null,
    val code: String? = null,
    var createdAt: String? = null,
    val storeId: String? = null,
    val storeIcon:String? = null,
    val storeName: String? = null,
    val email: String? = null,
    var status: String? = null
)
