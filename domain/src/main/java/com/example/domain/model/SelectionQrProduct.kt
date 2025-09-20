package com.example.domain.model

data class SelectionQrProduct(
    var selected : Boolean = false,
    var name:String,
    val id: String,
    var type: String,
    var image: String,
    var count:Int
)