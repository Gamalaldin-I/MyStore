package com.example.domain.repo

import com.example.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ProductRepo {

     fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    fun getAvailableProducts(): Flow<List<Product>>
    fun getArchiveProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product):Pair<Boolean,String>
    suspend fun updateProduct(product: Product): Pair<Boolean,String>
    suspend fun deleteProductById(id: String, image: String):Pair<Boolean,String>
    suspend fun isTheProductInTheStock(id:String): Boolean
    fun getArchiveLength(): Flow<Int>
    suspend fun fetchProductsFromRemoteIntoLocal():String
    fun listenToRemoteChanges(coroutineScope: CoroutineScope)


     //for pending products
    suspend fun addPendingProducts(listOfPending: List<Product>,
                                   onProgress: (progress:Int)->Unit,
                                   onFinish: ()->Unit)
    fun getPendingProducts(): Flow<List<Product>>
    suspend fun deletePendingProduct(id:String): Boolean


}