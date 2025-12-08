package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id:String,
    val createdAt:String,
    val deleted: Boolean,
    val userId:String,
    val userImage:String,
    val storeId:String,
    val productId:String,
    val productName:String,
    val billId:String,
    val type:String,
    val description:String,
)