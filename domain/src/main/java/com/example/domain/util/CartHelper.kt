package com.example.domain.util

import com.example.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartHelper {

    private val _cartList = MutableStateFlow(mutableListOf<Product>())
    val cartList: StateFlow<List<Product>> = _cartList.asStateFlow()

    fun addToTheCartList(product: Product) {
        val currentList = _cartList.value.toMutableList()
        if (currentList.any { it.id == product.id }) return
        currentList.add(product)
        _cartList.value = currentList
    }

    fun removeFromTheCartList(productId: String) {
        val currentList = _cartList.value.toMutableList()
        val itemToRemove = currentList.find { it.id == productId } ?: return
        currentList.remove(itemToRemove)
        _cartList.value = currentList
    }

    fun clearCartList() {
        _cartList.value = mutableListOf()
    }
}
