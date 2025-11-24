package com.example.domain.model

import com.example.domain.util.Constants

data class PendingSellAction(
    val id: Int = 0,
    val billId: String = "",
    val status: String = Constants.STATUS_PENDING,
    val soldProducts: List<CartProduct>,
    val discount:Int=0,
    val progress:Int=0,
    var billInserted:Boolean= false,
    var soldItemsInserted:Boolean = false
)