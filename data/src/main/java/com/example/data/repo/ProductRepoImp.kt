package com.example.data.repo

import com.example.data.Mapper.toData
import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ProductDao
import com.example.data.local.model.entities.ProductEntity
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import java.io.File

class ProductRepoImp (private val productDao: ProductDao): ProductRepo {

    override suspend fun getProducts(): List<Product> =
        productDao.getProducts().mapData()

    override suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()


    override suspend fun getAvailableProducts(): List<Product> =
        productDao.getAvailableProducts().mapData()

    override suspend fun getArchiveProducts(): List<Product> =
        productDao.getArchiveProducts().mapData()

    override suspend fun addProduct(product: Product) =
        productDao.addProduct(product.toData())

    override suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product.toData())

    override suspend fun deleteProductById(id: String, image: String) {
        productDao.deleteProductById(id)
        if(File(image).exists()){
            File(image).delete()
        }
    }

    override suspend fun getLowStock(): List<Product> =
        productDao.getLowStock().mapData()

    override suspend fun getTop5InSales(): List<Product> =
        productDao.getTop5InSales().mapData()

    fun List<ProductEntity>.mapData():List<Product> = this.map{it.toDomain()}

}