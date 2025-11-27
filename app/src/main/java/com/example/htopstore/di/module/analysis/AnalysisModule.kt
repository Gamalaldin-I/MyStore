package com.example.htopstore.di.module.analysis

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.repo.AnalysisRepoImp
import com.example.domain.repo.AnalysisRepo
import com.example.domain.useCase.analisys.GetDaysOfWorkUseCase
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetSpecificDayUseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalysisModule {
    @Provides
    @Singleton
    fun provideAnalysisRepo(expenseDao: ExpenseDao,productDao: ProductDao,salesDao: SalesDao): AnalysisRepo {
        return AnalysisRepoImp(productDao = productDao, expenseDao = expenseDao, salesDao = salesDao)
    }
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