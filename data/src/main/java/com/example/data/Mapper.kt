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
            sellingPrice = sellingPrice
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
            sellingPrice = sellingPrice
        )
    }

    //soldProduct
    fun SoldProduct.toSoldProductEntity(): SoldProductEntity {
        return SoldProductEntity(
            detailId = this.detailId,
            saleId = this.saleId,
            productId = this.productId,
            name = this.name,
            type = this.type,
            quantity = this.quantity,
            price = this.price,
            sellingPrice = this.sellingPrice,
            sellDate = this.sellDate,
            sellTime = this.sellTime
        )
    }

    fun SoldProductEntity.toSoldProduct(): SoldProduct {
        return SoldProduct(
            detailId = this.detailId,
            saleId = this.saleId,
            productId = this.productId,
            name = this.name,
            type = this.type,
            quantity = this.quantity,
            price = this.price,
            sellingPrice = this.sellingPrice,
            sellDate = this.sellDate,
            sellTime = this.sellTime
        )
    }

    //expense
    fun Expense.toExpenseEntity(): ExpenseEntity {
        return ExpenseEntity(
            expenseId = this.expenseId,
            date = this.date,
            time = this.time,
            description = this.description,
            category = this.category,
            amount = this.amount,
            paymentMethod = this.paymentMethod
        )
    }

    fun ExpenseEntity.toExpense(): Expense {
        return Expense(
            expenseId = this.expenseId,
            date = this.date,
            time = this.time,
            description = this.description,
            category = this.category,
            amount = this.amount,
            paymentMethod = this.paymentMethod
        )
    }

    //bill
    fun Bill.toBillEntity(): BillEntity {
        return BillEntity(
            saleId = this.saleId,
            discount = this.discount,
            date = this.date,
            time = this.time,
            totalCash = this.totalCash
        )
    }

    fun BillEntity.toBill(): Bill {
        return Bill(
            saleId = this.saleId,
            discount = this.discount,
            date = this.date,
            time = this.time,
            totalCash = this.totalCash
        )
    }

    fun SalesOpsWithDetails.toBillWithDetails(): BillWithDetails {
        return BillWithDetails(
            bill = this.saleOp.toBill(),
            soldProducts = this.soldProducts.map { it.toSoldProduct() }
        )
    }

}