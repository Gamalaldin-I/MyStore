package com.example.domain.util

import com.example.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartHelper {

    private val _cartList = MutableStateFlow(mutableListOf<Product>())
    val cartList: StateFlow<List<Product>> = _cartList.asStateFlow()

    fun addToTheCartList(product: Product) {
        if (product.count <= 0) return

        val list = _cartList.value.toMutableList()
        val index = list.indexOfFirst { it.id == product.id }

        if (index != -1) {
            val item = list[index]

            if (item.soldCount < product.count) {
                list[index] = item.copy(soldCount = item.soldCount + 1)
            }

        } else {
            list.add(product.copy(soldCount = 1))
        }

        _cartList.value = list
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
