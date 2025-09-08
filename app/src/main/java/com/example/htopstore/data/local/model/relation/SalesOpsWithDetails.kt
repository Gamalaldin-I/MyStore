package com.example.htopstore.data.local.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct

data class SalesOpsWithDetails(
    @Embedded val saleOp: SellOp,
    @Relation(
        parentColumn = "saleId",
        entityColumn = "saleId"
    )
    val soldProducts: List<SoldProduct>
)