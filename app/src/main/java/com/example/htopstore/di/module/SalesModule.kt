package com.example.htopstore.di.module

import com.example.domain.repo.SalesRepo
import com.example.domain.useCase.sales.GetAllSalesAndReturnsByDateUseCase
import com.example.domain.useCase.sales.GetAllSalesAndReturnsUseCase
import com.example.domain.useCase.sales.GetReturnsByDateUseCase
import com.example.domain.useCase.sales.GetReturnsUseCase
import com.example.domain.useCase.sales.GetSoldOnlyByDateUseCase
import com.example.domain.useCase.sales.GetSoldOnlyUseCase
import com.example.domain.useCase.sales.SellUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SalesModule {
    @Provides
    fun provideSellUseCase(salesRepo: SalesRepo): SellUseCase {
        return SellUseCase(salesRepo)
    }
    @Provides
    fun provideGetAllSalesAndReturnsUseCase(salesRepo: SalesRepo): GetAllSalesAndReturnsUseCase {
        return GetAllSalesAndReturnsUseCase(salesRepo)
    }

    @Provides
    fun provideGetAllSalesAndReturnsByDateUseCase(salesRepo:SalesRepo): GetAllSalesAndReturnsByDateUseCase {
        return GetAllSalesAndReturnsByDateUseCase(salesRepo)
    }

    @Provides
    fun provideGetSoldOnlyUseCase(salesRepo:SalesRepo): GetSoldOnlyUseCase {
        return GetSoldOnlyUseCase(salesRepo)
    }

    @Provides
    fun provideGetSoldOnlyByDateUseCase(salesRepo:SalesRepo): GetSoldOnlyByDateUseCase {
        return GetSoldOnlyByDateUseCase(salesRepo)
    }

    @Provides

    fun provideGetReturnsUseCase(salesRepo: SalesRepo): GetReturnsUseCase {
        return GetReturnsUseCase(salesRepo)
    }

    @Provides
    fun provideGetReturnsByDateUseCase(salesRepo:SalesRepo): GetReturnsByDateUseCase {
        return GetReturnsByDateUseCase(salesRepo)
    }
}