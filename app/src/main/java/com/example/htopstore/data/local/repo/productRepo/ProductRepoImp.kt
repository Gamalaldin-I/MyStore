package com.example.htopstore.data.local.repo.productRepo

import android.content.Context
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProductRepoImp(context: Context): ProductRepo {
    private val productDao = AppDataBase.getDatabase(context).productDao()

    // operations on products
    override suspend fun insertProduct(product: Product) {
        withContext(Dispatchers.IO){
        productDao.insertProduct(product)
        }
    } // insert single product
    override suspend fun insertProducts(products: List<Product>) {
        withContext(Dispatchers.IO) {
            productDao.insertProducts(products)
        }
    }// insert multiple products

    //get
    override suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {  // all
        productDao.getAllProducts()
    }
    override suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) { // id
        productDao.getProductById(id)
    }
    override suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        // category
        productDao.getProductsByCategory(category)
    }
    override suspend fun getProductsByName(name: String): List<Product> = withContext(Dispatchers.IO) {
        // name
        productDao.getProductsByName(name)
    }
    override suspend fun getProductsByDate(date: String): List<Product> = withContext(Dispatchers.IO) {
        // date
        productDao.getProductsByDate(date)
    }

    //delete
    override suspend fun deleteProductById(id: String,image:String) {
        withContext(Dispatchers.IO) {
            productDao.deleteProductById(id)
        }
        //delete the pic file
        val file = File(image)
        if (file.exists()){
        file.delete()
        }
    }
    override suspend fun deleteAllProducts() {
        withContext(Dispatchers.IO) {
            productDao.deleteAllProducts()
        }
    }

    override suspend fun updateProductQuantity(id: String, quantity: Int) {
        withContext(Dispatchers.IO) {
            productDao.updateProductQuantity(id, quantity)
        }

    }

    override suspend fun getProductsAvailable(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getProductsAvailable()
        }


    override suspend fun getProductsNotAvailable(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getProductsNotAvailable()
        }

    override suspend fun getArchiveLength(): Int =
        withContext(Dispatchers.IO) {
            productDao.getNotAvailableLength()
        }


    override suspend fun getProductsByNewest(): List<Product> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProductsSortedByDate()
        }

    }
}