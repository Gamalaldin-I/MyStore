package com.example.htopstore.di.module.analysis

import com.example.domain.repo.AnalysisRepo
import com.example.domain.useCase.analisys.GetDaysOfWorkUseCase
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetSpecificDayUseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import com.example.domain.useCase.analisys.product.GetLowStockUseCase
import com.example.domain.useCase.analisys.product.GetTop5UseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AnalysisModule {
    @Provides
    fun provideGetTotalExpensesByDateUseCase(repo: AnalysisRepo): GetTotalExpensesByDateUseCase {
        return GetTotalExpensesByDateUseCase(repo)
    }
    @Provides
    fun provideGetTotalSalesByDateUseCase(repo: AnalysisRepo): GetTotalSalesByDateUseCase {
        return GetTotalSalesByDateUseCase(repo)
    }
    @Provides
    fun provideGetProfitByDayUseCase(repo: AnalysisRepo): GetProfitByDayUseCase {
        return GetProfitByDayUseCase(repo)
    }

    @Provides
    fun provideGetDaysOfSalesUseCase(repo: AnalysisRepo): GetDaysOfWorkUseCase {
        return GetDaysOfWorkUseCase(repo)
    }
    @Provides
    fun provideGetSpecificDayUseCase(repo: AnalysisRepo): GetSpecificDayUseCase {
        return GetSpecificDayUseCase(repo)
    }



}