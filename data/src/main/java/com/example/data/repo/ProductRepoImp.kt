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

    override suspend fun addProduct(product: Product): Pair<Boolean, String> {
        var res = ""
        remote.addProduct(product){
                if(it != null) {
                    res = it.id
                    productDao.addProduct(it.toData())
                }
            }
        if(res.isEmpty()){
            return Pair(false,"check your internet connection")
        }else{
            return Pair(true,"Product added successfully")
        }
    }

    override suspend fun updateProduct(product: Product):Pair<Boolean,String>{
        var res =""
        remote.updateProduct(product){
            if(it != null){
                res = it.id
                productDao.updateProduct(it.toData())
            }
        }
        if(res.isEmpty()){
            return Pair(false,"check your internet connection")
        }
        return Pair(true,"Product updated successfully")
    }

    override suspend fun deleteProductById(id: String, image: String):Pair<Boolean,String> {
        return remote.deleteProduct(id){
           productDao.deleteProductById(id)
       }
    }

    override suspend fun isTheProductInTheStock(id: String): Boolean {
        val res = productDao.isProductInStock(id)?:0
        return res > 0
    }


    override  fun getArchiveLength(): Flow<Int> {
        return productDao.getArchiveLength()
    }

    override suspend fun fetchProductsFromRemoteIntoLocal():String {
        val (products,msg) =remote.observeAllProductsWithLastUpdateAndDeleted()
        products.forEach {
            if(it.deleted){
                productDao.deleteProductById(it.id)
            }else{
                productDao.addProduct(it.toData())
            }
        }
        return msg
    }

    override fun listenToRemoteChanges(
        coroutineScope: CoroutineScope
    ) {

    }

    fun Flow<List<ProductEntity>>.mapData():Flow<List<Product>> {
        return this.map {
            it.map {
                it.toDomain()
            }
        }
    }

}