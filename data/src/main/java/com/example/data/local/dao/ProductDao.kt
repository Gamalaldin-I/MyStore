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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducts(products: List<ProductEntity>)


    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteProductById(id: String)


    @Query("UPDATE product SET count = count + :quantity , soldCount = soldCount - :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterReturn(id: String, quantity: Int)

    @Query("UPDATE product SET count = count - :quantity , soldCount = soldCount + :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterSale(id: String, quantity: Int)

    @Query("SELECT COUNT(*) FROM product WHERE count = 0")
     fun getArchiveLength(): Flow<Int>

     @Query("SELECT count FROM product WHERE id = :id")
     suspend fun isProductInStock(id: String): Int?

     /**analytic queries*/
    //for product analysis
     @Query("SELECT * FROM product WHERE count < 5 AND count > 0 order by count")
     fun getLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE soldCount > 0 ORDER BY soldCount DESC LIMIT 10 ")
    fun getTop10InSales(): Flow<List<ProductEntity>>

    //get the products with highest profit
    @Query("""
    SELECT *
    FROM Product
    where count > 0 AND soldCount > 0
    ORDER BY (sellingPrice - buyingPrice) * soldCount  DESC
    LIMIT 10
""")
    fun getProductsByHighestProfit(): List<ProductEntity>

    //have not sold
    @Query("""
    SELECT *
    FROM Product
    where soldCount = 0 and count > 0
""")
    fun getProductsThatHaveNotBeenSold(): List<ProductEntity>



    //////////////////////////////////////////////////////////
    /////////////////////FOR PENDING PRODUCTS////////////////
    ////////////////////////////////////////////////////////
    @Query(
        """
        SELECT * 
        FROM product
        where pending is 1
    """
    )
    fun getAllPendingProducts(): Flow<List<ProductEntity>>


    @Query(
        """
        Update product set pending = 0 , productImage =:imageUrl where id =:id
    """
    )
    fun updateFromPendingToActive(id:String,imageUrl:String)




}