package com.example.htopstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.htopstore.data.local.model.Expense
@Dao
interface ExpenseDao {
    /**home Fragment queries*/
    //get the expenses of the day
    @Query("SELECT SUM(amount) FROM expenses WHERE date = :date")
    suspend fun getExpensesToday(date: String): Double?




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>
    @Update
    suspend fun updateExpense(expense: Expense)


}