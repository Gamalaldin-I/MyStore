package com.example.htopstore.data.local.repo.stock

import com.example.htopstore.data.local.model.Product

interface StockRepo {
    suspend fun getAvailableProducts(): List<Product>
}