package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
     fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM product WHERE count > 0 order by addingDate DESC")
     fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE count = 0 order by addingDate")
     fun getArchiveProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT * FROM product WHERE count < 5 AND count > 0 order by addingDate")
     fun getLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE soldCount > 0 ORDER BY soldCount DESC LIMIT 5 ")
     fun getTop5InSales(): Flow<List<ProductEntity>>

    @Query("UPDATE product SET count = count + :quantity , soldCount = soldCount - :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterReturn(id: String, quantity: Int)

    @Query("UPDATE product SET count = count - :quantity , soldCount = soldCount + :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterSale(id: String, quantity: Int)

    @Query("SELECT COUNT(*) FROM product WHERE count = 0")
     fun getArchiveLength(): Flow<Int>

     @Query("SELECT count FROM product WHERE id = :id")
     suspend fun isProductInStock(id: String): Int?
}