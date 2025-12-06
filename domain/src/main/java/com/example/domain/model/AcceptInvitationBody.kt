package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AcceptInvitationBody(
    val status: String,
    val acceptedAt:String,
    val role:Int
)