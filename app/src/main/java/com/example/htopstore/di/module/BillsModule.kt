package com.example.htopstore.di.module

import com.example.domain.repo.BillRepo
import com.example.domain.useCase.bill.GetAllBillsUseCase
import com.example.domain.useCase.bill.GetBillByDateUseCase
import com.example.domain.useCase.bill.GetBillsByDateRangeUseCase
import com.example.domain.useCase.bill.GetBillsTillDateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillsModule {

    @Provides
    @Singleton
    fun provideGetAllBillsUseCase(billRepo: BillRepo): GetAllBillsUseCase {
        return GetAllBillsUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsByDateUseCase(billRepo: BillRepo): GetBillByDateUseCase {
        return GetBillByDateUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsByDateRangeUseCase(billRepo: BillRepo): GetBillsByDateRangeUseCase {
        return GetBillsByDateRangeUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsTillDateUseCase(billRepo: BillRepo): GetBillsTillDateUseCase {
        return GetBillsTillDateUseCase(billRepo)
    }
}