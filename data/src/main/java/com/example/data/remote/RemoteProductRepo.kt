package com.example.data.remote

import com.example.domain.model.Product

interface RemoteProductRepo {
    fun addListOfProducts(products: List<Product>)
    fun addProduct(product: Product)
    fun updateProduct(product: Product)
    fun deleteProductById(id: String)
    fun getProducts(): List<Product>
}