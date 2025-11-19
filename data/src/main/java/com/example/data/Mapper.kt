package com.example.data

import com.example.data.local.model.entities.BillEntity
import com.example.data.local.model.entities.ExpenseEntity
import com.example.data.local.model.entities.ProductEntity
import com.example.data.local.model.entities.SoldProductEntity
import com.example.data.local.model.relation.SalesOpsWithDetails
import com.example.domain.model.Bill
import com.example.domain.model.BillWithDetails
import com.example.domain.model.Expense
import com.example.domain.model.Product
import com.example.domain.model.SoldProduct

object Mapper {
    fun ProductEntity.toDomain(): Product {
        return Product(
            id = id,
            productImage = productImage,
            addingDate = addingDate,
            category = category,
            name = name,
            count = count,
            soldCount = soldCount,
            buyingPrice = buyingPrice,
            sellingPrice = sellingPrice,
            lastUpdate = lastUpdate,
            storeId = "",
            deleted = false
        )
    }

    fun Product.toData(): ProductEntity {
        return ProductEntity(
            id = id,
            productImage = productImage,
            addingDate = addingDate,
            category = category,
            name = name,
            count = count,
            soldCount = soldCount,
            buyingPrice = buyingPrice,
            sellingPrice = sellingPrice,
            lastUpdate = lastUpdate
        )
    }

    //soldProduct
    fun SoldProduct.toSoldProductEntity(): SoldProductEntity {
        return SoldProductEntity(
            detailId = this.id,
            saleId = this.billId,
            productId = this.productId,
            name = this.name,
            type = this.type,
            quantity = this.quantity,
            price = this.price,
            sellingPrice = this.sellingPrice,
            sellDate = this.sellDate,
            sellTime = this.sellTime,
            lastUpdate = this.lastUpdate
        )
    }

    fun SoldProductEntity.toSoldProduct(): SoldProduct {
        return SoldProduct(
            id = this.detailId,
            billId = this.saleId,
            productId = this.productId,
            name = this.name,
            type = this.type,
            quantity = this.quantity,
            price = this.price,
            sellingPrice = this.sellingPrice,
            sellDate = this.sellDate,
            sellTime = this.sellTime,
            lastUpdate = this.lastUpdate
        )
    }

    //expense
    fun Expense.toExpenseEntity(): ExpenseEntity {
        return ExpenseEntity(
            expenseId = this.id,
            date = this.date,
            time = this.time,
            description = this.description,
            category = this.category,
            amount = this.amount,
            paymentMethod = this.paymentMethod,
            lastUpdate = this.lastUpdate
        )
    }

    fun ExpenseEntity.toExpense(): Expense {
        return Expense(
            id = this.expenseId,
            date = this.date,
            time = this.time,
            description = this.description,
            category = this.category,
            amount = this.amount,
            paymentMethod = this.paymentMethod,
            lastUpdate = this.lastUpdate,
            storeId = ""
        )
    }

    //bill
    fun Bill.toBillEntity(): BillEntity {
        return BillEntity(
            saleId = this.id,
            discount = this.discount,
            date = this.date,
            time = this.time,
            totalCash = this.totalCash,
            lastUpdate = this.lastUpdate
        )
    }

    fun BillEntity.toBill(): Bill {
        return Bill(
            id = this.saleId,
            discount = this.discount,
            date = this.date,
            time = this.time,
            totalCash = this.totalCash,
            lastUpdate = this.lastUpdate,
            storeId = ""
        )
    }

    fun SalesOpsWithDetails.toBillWithDetails(): BillWithDetails {
        return BillWithDetails(
            bill = this.saleOp.toBill(),
            soldProducts = this.soldProducts.map { it.toSoldProduct() }
        )
    }

}