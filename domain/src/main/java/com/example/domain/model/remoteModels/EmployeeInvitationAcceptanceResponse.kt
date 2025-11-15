package com.example.domain.model.remoteModels

import kotlinx.serialization.Serializable

@Serializable
class EmployeeInvitationAcceptanceResponse(
    val storeId:String,
    val role:Int,
    val status:String
)