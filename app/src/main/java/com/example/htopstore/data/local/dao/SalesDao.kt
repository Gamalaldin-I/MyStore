package com.example.htopstore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
@Dao
interface SalesDao {
    /**Home fragment*/
    //get the total sales of the day (total income)
    @Query("SELECT SUM(sellingPrice * quantity) FROM sales_details WHERE sellDate = :date and saleId is not null")
    suspend fun getTotalSalesOfToday(date: String): Double?
    // get the profit of the day
    @Query("Select SUM((sellingPrice - price) * quantity) from sales_details where sellDate = :date and saleId is not null")
    suspend fun getProfitOfToday(date: String): Double?



    /**Bills Activity*/
    //get all bills in the day
    @Query("SELECT * FROM sell_ops WHERE date = :date order by time desc")
    suspend fun getBillsByDate(date: String): List<SellOp>
    //get all bills
    @Query("SELECT * FROM sell_ops order by date,time desc")
    suspend fun getAllBills(): List<SellOp>
    //get all bills in the range
    @Query("SELECT * FROM sell_ops WHERE date BETWEEN :since AND :to order by date,time desc")
    suspend fun getBillsByDateRange(since: String, to:String): List<SellOp>
    //get all bills till date
    @Query("SELECT * FROM sell_ops WHERE date <= :date order by date,time desc")
    suspend fun getBillsTillDate(date: String): List<SellOp>

    /**Bill Details Activity*/

    // get bill with details
    @Transaction
    @Query("SELECT * FROM sell_ops WHERE saleId = :id")
    suspend fun getBillWithDetails(id:String): SalesOpsWithDetails

    //update product quantity after return
    @Query("Update sell_ops set totalCash = totalCash - :returnCash where saleId = :id")
    suspend fun updateSaleCashAfterReturn(id: String, returnCash: Double)

    //update product quantity after return
    @Query("Update sales_details set quantity = quantity - :quantity where detailId = :id")
    suspend fun updateBillProductQuantityAfterReturn( id:String, quantity: Int)

    @Delete
    suspend fun deleteSoldProduct(soldProduct: SoldProduct)

    //delete the sale
    @Query("DELETE FROM sell_ops WHERE saleId = :saleId")
    suspend fun deleteSaleById(saleId: String)











    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleOp(saleOp: SellOp)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldProducts(soldProducts: List<SoldProduct>)

    //insert return
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldProduct(returns: SoldProduct)




    @Transaction
    @Query("SELECT * FROM sell_ops WHERE saleId = :saleId")
    suspend fun getSalesOpWithDetails(saleId: String): SalesOpsWithDetails?

    // get all the sales ops with details
    @Transaction
    @Query("SELECT * FROM sell_ops  order by date,time desc")
    suspend fun getAllSalesOpsWithDetails(): List<SalesOpsWithDetails>
    // get all the sales ops with details with date
    @Transaction
    @Query("SELECT * FROM sell_ops WHERE date = :date order by time desc" )
    suspend fun getAllSalesOpsWithDetailsByDate(date: String): List<SalesOpsWithDetails>

    @Update
    suspend fun updateSoldProduct(soldProduct: SoldProduct)





    //get for Sales Activity
    @Query("SELECT * FROM sales_details where saleId is not null order by sellDate desc , sellTime desc")
    suspend fun getAllSalesAndReturns(): List<SoldProduct>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE sellDate = :date  and saleId is not null order by sellTime desc")
    suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProduct>

    //getReturns
    @Query("SELECT * FROM sales_details WHERE quantity <0 order by sellDate desc, sellTime desc")
    suspend fun getReturns(): List<SoldProduct>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE quantity <0 AND sellDate = :date order by sellTime desc")
    suspend fun getReturnsByDate(date: String): List<SoldProduct>

    //getSales
    @Query("SELECT * FROM sales_details WHERE quantity > 0 AND saleId IS NOT NULL ORDER BY sellDate DESC, sellTime DESC")
    suspend fun getSales(): List<SoldProduct>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE quantity > 0 AND saleId IS NOT NULL AND sellDate = :date ORDER BY sellTime DESC")
    suspend fun getSalesByDate(date: String): List<SoldProduct>
}