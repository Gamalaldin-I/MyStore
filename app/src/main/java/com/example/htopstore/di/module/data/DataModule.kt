package com.example.htopstore.di.module.data

import android.content.Context
import androidx.room.Room
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.PendingSellDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.local.roomDb.AppDataBase
import com.example.data.local.sharedPrefs.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDataBase {
        return Room.databaseBuilder(
            context,
            AppDataBase::class.java,
            AppDataBase.Companion.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPref {
        return SharedPref(context)
    }

    @Provides
    @Singleton
    fun provideProductDao(db: AppDataBase): ProductDao {
        return db.productDao()
    }
    @Provides
    @Singleton
    fun providePendingSellActionDao(db: AppDataBase): PendingSellDao {
        return db.pendingSellDao()
    }
    @Provides
    @Singleton
    fun provideSalesDao(db: AppDataBase): SalesDao {
        return db.salesDao()
    }
    @Provides
    @Singleton
    fun provideExpenseDao(db: AppDataBase): ExpenseDao {
        return db.expenseDao()
    }
}