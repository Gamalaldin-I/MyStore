package com.example.htopstore.data.local.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.htopstore.data.local.dao.ExpenseDao
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.dao.ProductDao
import com.example.htopstore.data.local.dao.SalesDao
import com.example.htopstore.data.local.model.Expense
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.data.local.model.SoldProduct

@Database(entities = [Product::class, SellOp::class, SoldProduct::class, Expense::class], version = 1, exportSchema = false)
abstract class AppDataBase(): RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun salesDao(): SalesDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null
        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "HTOP_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}