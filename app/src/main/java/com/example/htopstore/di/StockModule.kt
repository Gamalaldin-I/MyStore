package com.example.htopstore.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.htopstore.data.local.repo.stock.StockRepoImp
import com.example.htopstore.domain.useCase.stock.GetStockProductsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object StockModule {
    @Provides
    fun provideStockRepo(@ApplicationContext context: Context): StockRepoImp{
        return StockRepoImp(context)
    }

    @Provides
    fun provideStockProducts(stockRepoImp: StockRepoImp): GetStockProductsUseCase{
        return GetStockProductsUseCase(stockRepoImp)
    }
}