package com.example.domain.repo

import com.example.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ProductRepo {

     fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
     fun getAvailableProducts(): Flow<List<Product>>
     fun getArchiveProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProductById(id: String, image: String)
    suspend fun isTheProductInTheStock(id:String): Boolean
    fun getArchiveLength(): Flow<Int>
    suspend fun fetchProductsFromRemoteIntoLocal()
    fun listenToRemoteChanges(coroutineScope: CoroutineScope)
}