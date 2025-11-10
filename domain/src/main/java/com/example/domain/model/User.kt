package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
 data class User(
   var id: String,
   var name: String,
   var role:Int,
   val photoUrl:String,
   val status: String,
   val storeId:String,
   var email:String,
   var provider:Int = 0
)