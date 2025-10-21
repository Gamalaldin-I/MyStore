package com.example.domain.model

data class User(
    var id: String,
    var name: String,
    var role:Int,
    var email:String,
    var password:String
)