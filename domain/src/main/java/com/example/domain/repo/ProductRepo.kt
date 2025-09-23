package com.example.domain.repo

import com.example.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepo {
     fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
     fun getAvailableProducts(): Flow<List<Product>>
     fun getArchiveProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProductById(id: String, image: String)
    fun getArchiveLength(): Flow<Int>
}