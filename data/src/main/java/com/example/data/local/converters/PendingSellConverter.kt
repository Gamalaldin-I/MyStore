package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.domain.model.CartProduct
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PendingSellConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromCartProductList(products: List<CartProduct>?): String? {
        return gson.toJson(products)
    }

    @TypeConverter
    fun toCartProductList(data: String?): List<CartProduct>? {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CartProduct>>() {}.type
        return gson.fromJson(data, listType)
    }
}
