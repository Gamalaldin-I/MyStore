package com.example.data.local.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.PendingSellConverter
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.PendingSellDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.local.model.entities.BillEntity
import com.example.data.local.model.entities.ExpenseEntity
import com.example.data.local.model.entities.PendingSellActionEntity
import com.example.data.local.model.entities.ProductEntity
import com.example.data.local.model.entities.SoldProductEntity

@Database(entities =
    [   ProductEntity::class,
        BillEntity::class,
        SoldProductEntity::class,
        ExpenseEntity::class,
        PendingSellActionEntity::class
    ],
    version =1, exportSchema = false
    )
@TypeConverters(PendingSellConverter::class)
abstract class AppDataBase(): RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun salesDao(): SalesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun pendingSellDao(): PendingSellDao


    companion object {
        const val DATABASE_NAME = "HTOP_database"
        @Volatile
        private var INSTANCE: AppDataBase? = null
        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
        }
}