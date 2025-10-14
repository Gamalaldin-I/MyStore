package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.entities.ExpenseEntity
import com.example.domain.model.ExpensesWithCategory
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


    @Query("""
        Select amount,category
        from expenses 
        where date between :start and :end
        group by category
        order by amount
    """)
    suspend fun getTheExpensesByCategories(start: String, end:String): List<ExpensesWithCategory>

    @Query("""
        Select SUM(amount) from expenses 
        where date between :start and :end
    """)
    suspend fun getTheTotalOfExpensesByRange(start: String,end: String): Double?

}