package com.example.domain.repo

import com.example.domain.model.Product

interface ProductRepo {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
    suspend fun getAvailableProducts(): List<Product>
    suspend fun getArchiveProducts(): List<Product>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProductById(id: String, image: String)
    suspend fun getLowStock(): List<Product>
    suspend fun getTop5InSales(): List<Product>
}