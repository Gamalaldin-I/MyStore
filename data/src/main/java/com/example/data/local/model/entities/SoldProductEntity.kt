package com.example.data.local.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_details",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["saleId"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SoldProductEntity(
    @PrimaryKey
    var detailId: String,
    var saleId: String?,
    val productId: String?,
    val name: String,
    val type: String,
    var quantity: Int,
    val price: Double,
    val sellingPrice: Double,
    val sellDate: String,
    val sellTime: String,
    var lastUpdate:String
)