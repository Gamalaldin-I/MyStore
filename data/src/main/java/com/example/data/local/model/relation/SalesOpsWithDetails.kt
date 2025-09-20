package com.example.data.local.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.model.entities.BillEntity
import com.example.data.local.model.entities.SoldProductEntity

data class SalesOpsWithDetails(
    @Embedded val saleOp: BillEntity,
    @Relation(
        parentColumn = "saleId",
        entityColumn = "saleId"
    )
    val soldProducts: List<SoldProductEntity>
)