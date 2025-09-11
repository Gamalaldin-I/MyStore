package com.example.htopstore.data.local.repo.product

import com.example.htopstore.data.local.model.Product

interface ProductRepo {
    suspend fun getProductById(id: String): Product?
    suspend fun updateProduct(product: Product)
    suspend fun deleteProductById(id: String,image: String)
}