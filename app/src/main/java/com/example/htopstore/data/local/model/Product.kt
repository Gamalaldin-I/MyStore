package com.example.htopstore.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = false)
    val id:String,
    val productImage:String,
    val addingDate:String,
    var category:String,
    var name:String,
    var count:Int,
    var soldCount:Int,
    var buyingPrice:Double,
    var sellingPrice:Double,
)