package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    /**home Fragment queries*/
    //get the expenses of the day
    @Query("SELECT SUM(amount) FROM expenses WHERE date = :date")
      fun getTotalExpensesForDate(date: String): Flow<Double?>




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<ExpenseEntity>
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    @Query("DELETE FROM expenses WHERE expenseId = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT * FROM expenses WHERE date = :date")
    suspend fun getExpensesListByDate(date: String): List<ExpenseEntity>




}