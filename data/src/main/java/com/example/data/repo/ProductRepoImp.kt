package com.example.data.repo

import com.example.data.Mapper.toData
import com.example.data.Mapper.toDomain
import com.example.data.local.dao.ProductDao
import com.example.data.local.model.entities.ProductEntity
import com.example.data.remote.repo.RemoteProductRepo
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepoImp (
    private val productDao: ProductDao,
    private val remote: RemoteProductRepo,
): ProductRepo {

    override  fun getProducts():Flow<List<Product>> {
       return productDao.getProducts().mapData()}

    override suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()

    override  fun getAvailableProducts(): Flow<List<Product>> =
         productDao.getAvailableProducts().mapData()

    override  fun getArchiveProducts(): Flow<List<Product>> =
        productDao.getArchiveProducts().mapData()

    override suspend fun addProduct(product: Product){
        remote.addProduct(product){
            productDao.addProduct(it.toData())
        }
    }

    override suspend fun updateProduct(product: Product) {
        remote.updateProduct(product){
            productDao.updateProduct(it.toData())
        }
    }

    override suspend fun deleteProductById(id: String, image: String) {
       remote.deleteProduct(id){
           productDao.deleteProductById(it)
       }
    }

    override suspend fun isTheProductInTheStock(id: String): Boolean {
        val res = productDao.isProductInStock(id)?:0
        return res > 0
    }


    override  fun getArchiveLength(): Flow<Int> {
        return productDao.getArchiveLength()
    }

    override suspend fun fetchProductsFromRemoteIntoLocal() {
        remote.getAllProducts() {
            productDao.addProducts(it.map { it.toData() })
        }
    }

    override fun listenToRemoteChanges(
        coroutineScope: CoroutineScope
    ) {
        remote.listenToProductChanges (
            scope = coroutineScope,
            onInsert = {
                productDao.addProduct(it.toData())
            },
            onUpdate = {
                productDao.updateProduct(it.toData())
            },
            onDelete = {
                productDao.deleteProductById(it.id)
            },
            onProductFoundInCache = {
                isTheProductInTheStock(it)
            }
        )
    }

    fun Flow<List<ProductEntity>>.mapData():Flow<List<Product>> {
        return this.map {
            it.map {
                it.toDomain()
            }
        }
    }

}