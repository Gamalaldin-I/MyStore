package com.example.data.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.CartProduct
import com.example.domain.util.Constants

@Entity(tableName = "pending_sell_operations")
data class PendingSellAction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val status: String = Constants.STATUS_PENDING,
    val soldProducts: List<CartProduct>,
    val discount:Int=0,
    val progress:Int=0,
    var billInserted:Boolean= false,
    var soldItemsInserted:Boolean = false
)