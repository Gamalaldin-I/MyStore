package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.entities.ProductEntity

@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
    suspend fun getProducts(): List<ProductEntity>

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM product WHERE count > 0")
    suspend fun getAvailableProducts(): List<ProductEntity>

    @Query("SELECT * FROM product WHERE count = 0")
    suspend fun getArchiveProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT * FROM product WHERE count < 5")
    suspend fun getLowStock(): List<ProductEntity>

    @Query("SELECT * FROM product ORDER BY soldCount DESC LIMIT 5")
    suspend fun getTop5InSales(): List<ProductEntity>

    @Query("UPDATE product SET count = count + :quantity , soldCount = soldCount - :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterReturn(id: String, quantity: Int)

    @Query("UPDATE product SET count = count - :quantity , soldCount = soldCount + :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterSale(id: String, quantity: Int)


}