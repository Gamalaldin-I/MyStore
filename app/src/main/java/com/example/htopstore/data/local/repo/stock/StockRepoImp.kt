package com.example.htopstore.data.local.repo.stock

import android.content.Context
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StockRepoImp @Inject constructor(context: Context): StockRepo{
    private val productDao = AppDataBase.getDatabase(context).productDao()
    override suspend fun getAvailableProducts(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getProductsAvailable()
        }

}