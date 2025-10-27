package com.example.domain.model.remoteModels

import com.example.domain.util.Constants.STATUS_PENDING

data class Employee(
    val id: String,
    var name: String,
    var email: String,
    var ownerId:String?,
    var storeId:String?,
    var acceptedAt:String?,
    var joinedAt:String?,
    var status:String? = STATUS_PENDING,
    var role:Int?
)
{
    constructor() : this("","","","","","","","",0)
}

