package com.example.htopstore.domain.model

import android.media.Image

data class SelectionQrProduct(
    var selected : Boolean = false,
    var name:String,
    val id: String,
    var type: String,
    var image: String,
    var count:Int
)