package com.example.data.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class ProductEntity(
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
    var lastUpdate:String
)