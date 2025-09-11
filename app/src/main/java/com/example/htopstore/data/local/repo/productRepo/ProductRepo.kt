package com.example.htopstore.data.local.repo.productRepo

import com.example.htopstore.data.local.model.Product

interface ProductRepo {
    // operations on products
    // insert
    suspend fun insertProduct(product: Product)
    suspend fun insertProducts(products: List<Product>)

    //get
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductsByCategory(category: String): List<Product>
    suspend fun getProductsByName(name: String): List<Product>
    suspend fun getProductsByDate(date: String): List<Product>


    //delete
    suspend fun deleteAllProducts()

    //update quantity
    suspend fun updateProductQuantity(id: String, quantity: Int)



    //data for stock (available)
    suspend fun getProductsAvailable(): List<Product>
    // data for Archive (not available)
    suspend fun getProductsNotAvailable(): List<Product>
    suspend fun getArchiveLength(): Int


    // get products by newest
    suspend fun getProductsByNewest(): List<Product>

}