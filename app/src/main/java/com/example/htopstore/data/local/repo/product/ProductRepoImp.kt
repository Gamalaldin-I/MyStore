package com.example.htopstore.data.local.repo.product

import android.content.Context
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProductRepoImp(context: Context): ProductRepo  {
    private val productDao = AppDataBase.getDatabase(context).productDao()

    override suspend fun getProductById(id: String): Product?  =
        withContext(Dispatchers.IO){
            productDao.getProductById(id)
        }

    override suspend fun updateProduct(product: Product) {
        withContext(Dispatchers.IO){
            productDao.updateProduct(product)
        }
    }
    override suspend fun deleteProductById(id: String,image: String) = withContext(Dispatchers.IO){
        productDao.deleteProductById(id)
        //delete the pic file
        val file = File(image)
        if (file.exists()){
            file.delete()
        }
    }
}