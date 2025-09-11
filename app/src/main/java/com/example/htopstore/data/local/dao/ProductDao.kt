package com.example.htopstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.htopstore.data.local.model.Product

@Dao
interface ProductDao {
    /**home Fragment queries*/
    // get top 5 in sales
    @Query("SELECT * FROM product  where soldCount> 0 ORDER BY soldCount DESC LIMIT 5")
    suspend fun getTop5InSales(): List<Product>
    // get low stock
    @Query("SELECT * FROM product WHERE count < 5 and count > 0")
    suspend fun getLowStock(): List<Product>

    /**Archive Fragment queries*/
    //that is not available
    @Query("SELECT * FROM product WHERE count = 0")
    suspend fun getProductsNotAvailable(): List<Product>


    /**Stock Fragment queries*/
    //that is available
    @Query("SELECT * FROM product WHERE count > 0")
    suspend fun getProductsAvailable(): List<Product>

    /**Cart Fragment queries*/
    //update quantity
    @Query("UPDATE product SET soldCount = soldCount + :quantity , count = count - :quantity WHERE id = :id")
    suspend fun updateProductQuantity(id: String, quantity: Int)

    /**Add Activity queries*/
    //add product
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)


    /**Product Activity queries*/
    //get product by id
    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getProductById(id: String): Product?
    //update product
    @Update
    suspend fun updateProduct(product: Product)
    //delete product
    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteProductById(id: String)


    /**Bill Details Activity queries*/
    //update product quantity after return
    @Query("UPDATE product SET count = count + :quantity , soldCount = soldCount - :quantity WHERE id = :id")
    suspend fun updateProductQuantityAfterReturn(id: String, quantity: Int)






    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT * FROM product")
    suspend fun getAllProducts(): List<Product>



    @Query("DELETE FROM product")
    suspend fun deleteAllProducts()

    @Query("SELECT COUNT(*) FROM product")
    suspend fun getProductsCount(): Int

    @Query("SELECT * FROM product WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<Product>

    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductsByName(name: String): List<Product>

    @Query("SELECT * FROM product WHERE addingDate = :date")
    suspend fun getProductsByDate(date: String): List<Product>



    // products query by sold count And the archive , and the available

    // that is sold
    @Query("SELECT * FROM product WHERE soldCount > 0")
    suspend fun getProductsSold(): List<Product>
    //that is not sold even
    @Query("SELECT * FROM product WHERE soldCount = 0")
    suspend fun getProductsNotSold(): List<Product>

    //get the notAvailable length
    @Query("SELECT COUNT(*) FROM product WHERE count = 0")
    suspend fun getNotAvailableLength(): Int


    //get the capitulation of the products
    @Query("SELECT SUM(sellingPrice*count) FROM product")
    suspend fun getMoneyOfAllProducts(): Double

    @Query("SELECT SUM(count) FROM product")
    suspend fun getCountsOfProducts(): Int

    @Query("SELECT SUM(count) FROM product WHERE category = :category")
    suspend fun getStockOfProductsByCategory(category: String): Int





    // for qr selection view All products from th newest
    @Query("SELECT * FROM product ORDER BY addingDate DESC")
    suspend fun getAllProductsSortedByDate(): List<Product>


}