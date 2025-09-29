package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.model.entities.BillEntity
import com.example.data.local.model.entities.SoldProductEntity
import com.example.data.local.model.relation.SalesOpsWithDetails
import com.example.domain.model.CategorySales
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    /**Home fragment*/
    //get the total sales of the day (total income)
    @Query("SELECT SUM(sellingPrice * quantity) FROM sales_details WHERE sellDate = :date and saleId is not null")
      fun getTotalSalesOfToday(date: String): Flow<Double?>
    // get the profit of the day
    @Query("Select SUM((sellingPrice - price) * quantity) from sales_details where sellDate = :date and saleId is not null")
      fun getProfitOfToday(date: String): Flow<Double?>

    @Query("SELECT DISTINCT date FROM sell_ops ORDER BY date DESC LIMIT 60")
      suspend fun getWorkDays(): List<String>

    @Query("SELECT Distinct date FROM sell_ops WHERE date = :day")
      suspend fun getSpecificDay(day: String): String



    /**Bills Activity*/
    //get all bills in the day
    @Query("SELECT * FROM sell_ops WHERE date = :date order by time desc")
     fun getBillsByDate(date: String): Flow<List<BillEntity>>
    //get all bills
    @Query("SELECT * FROM sell_ops order by date desc ,time desc")
    suspend fun getAllBills(): List<BillEntity>
    //get all bills in the range
    @Query("SELECT * FROM sell_ops WHERE date BETWEEN :since AND :to order by date,time desc")
    suspend fun getBillsByDateRange(since: String, to:String): List<BillEntity>
    //get all bills till date
    @Query("SELECT * FROM sell_ops WHERE date <= :date order by date,time desc")
    suspend fun getBillsTillDate(date: String): List<BillEntity>

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
    suspend fun deleteSoldProduct(soldProduct: SoldProductEntity)

    //delete the sale
    @Query("DELETE FROM sell_ops WHERE saleId = :saleId")
    suspend fun deleteSaleById(saleId: String)











    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(saleOp: BillEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillDetails(soldProducts: List<SoldProductEntity>)

    //insert return
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldProduct(returns: SoldProductEntity)





    //get for Sales Activity
    @Query("SELECT * FROM sales_details where saleId is not null order by sellDate desc , sellTime desc")
    suspend fun getAllSalesAndReturns(): List<SoldProductEntity>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE sellDate = :date  and saleId is not null order by sellTime desc")
    suspend fun getAllSalesAndReturnsByDate(date: String): List<SoldProductEntity>

    //getReturns
    @Query("SELECT * FROM sales_details WHERE quantity <0 order by sellDate desc, sellTime desc")
    suspend fun getReturns(): List<SoldProductEntity>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE quantity <0 AND sellDate = :date order by sellTime desc")
     fun getReturnsByDate(date: String): Flow<List<SoldProductEntity>>

    //getSales
    @Query("SELECT * FROM sales_details WHERE quantity > 0 AND saleId IS NOT NULL ORDER BY sellDate DESC, sellTime DESC")
    suspend fun getSales(): List<SoldProductEntity>
    //filter by date
    @Query("SELECT * FROM sales_details WHERE quantity > 0 AND saleId IS NOT NULL AND sellDate = :date ORDER BY sellTime DESC")
    suspend fun getSalesByDate(date: String): List<SoldProductEntity>


    /**for analysis*/
    // Example for a specific range (startDate to endDate)
        @Query("""
        SELECT type, SUM(quantity) as totalSold
        FROM SALES_DETAILS
        WHERE sellDate BETWEEN :startDate AND :endDate AND saleId IS NOT NULL
        GROUP BY type
    """)
        fun getSellingCategoriesByDate(startDate: String, endDate: String): List<CategorySales>

        //get returns category
        @Query("""
        SELECT type, SUM(quantity) as totalSold
        FROM SALES_DETAILS
        WHERE sellDate BETWEEN :startDate AND :endDate AND saleId IS  NULL AND quantity < 0
        GROUP BY type
    """)
        fun getReturningCategoriesByDate(startDate: String, endDate: String): List<CategorySales>

    @Query("""
    SELECT type
    FROM SALES_DETAILS
    WHERE sellDate BETWEEN :startDate AND :endDate AND saleId IS NOT NULL
    GROUP BY type
    ORDER BY SUM(quantity) DESC
    LIMIT 1
""")
    fun getTheMostSellingCategoryByDate(startDate: String, endDate: String): String
    @Query("""
    SELECT type
    FROM SALES_DETAILS
    WHERE sellDate BETWEEN :startDate AND :endDate AND saleId IS NOT NULL
    GROUP BY type
    ORDER BY SUM(quantity) 
    LIMIT 1
""")
    fun getTheLeastSellingCategoryByDate(startDate: String, endDate: String): String



}