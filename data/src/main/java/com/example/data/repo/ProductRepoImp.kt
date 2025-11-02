package com.example.data.repo

import com.example.data.Mapper.toData
import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ProductDao
import com.example.data.local.model.entities.ProductEntity
import com.example.data.remote.repo.RemoteProductRepo
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class ProductRepoImp (
    private val productDao: ProductDao,
    private val remote: RemoteProductRepo
): ProductRepo {

    override  fun getProducts():Flow<List<Product>> {
       return productDao.getProducts().mapData()}

    override suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()


    override  fun getAvailableProducts(): Flow<List<Product>> =
         productDao.getAvailableProducts().mapData()


    override  fun getArchiveProducts(): Flow<List<Product>> =
        productDao.getArchiveProducts().mapData()

    override suspend fun addProduct(product: Product) {
        productDao.addProduct(product.toData())
        remote.addProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toData())
       // remote.updateProduct(product,context)
    }

    override suspend fun deleteProductById(id: String, image: String) {
        productDao.deleteProductById(id)
        if(File(image).exists()){
            File(image).delete()
        }
      //  remote.deleteProductById(id)
    }


    override  fun getArchiveLength(): Flow<Int> {
        return productDao.getArchiveLength()
    }

    override fun fetchProductsFromRemoteIntoLocal(products: List<Product>) {
        TODO("Not yet implemented")
    }

    override fun listenToRemoteChanges() {
        TODO("Not yet implemented")
    }

    fun Flow<List<ProductEntity>>.mapData():Flow<List<Product>> {
        return this.map {
            it.map {
                it.toDomain()
            }
        }
    }

}