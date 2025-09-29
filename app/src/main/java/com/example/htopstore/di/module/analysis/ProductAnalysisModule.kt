package com.example.htopstore.di.module.analysis

import com.example.domain.repo.AnalysisRepo
import com.example.domain.useCase.analisys.product.GetHaveNotSoldProductsUseCase
import com.example.domain.useCase.analisys.product.GetLowStockUseCase
import com.example.domain.useCase.analisys.product.GetReturningCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetSellingCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetTheHighestProfitProductsUseCase
import com.example.domain.useCase.analisys.product.GetTheLeastSellingCategoryUseCase
import com.example.domain.useCase.analisys.product.GetTheMostSellingCategoryUseCase
import com.example.domain.useCase.analisys.product.GetTop5UseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProductAnalysisModule {
    @Provides
    fun provideGetTop5UseCase(repo: AnalysisRepo): GetTop5UseCase {
        return GetTop5UseCase(repo)
    }
    @Provides
    fun provideGetLowStockUseCase(repo: AnalysisRepo): GetLowStockUseCase {
        return GetLowStockUseCase(repo)
    }
    @Provides
    fun provideSellingCategoryUseCase(repo: AnalysisRepo): GetSellingCategoriesUseCase {
        return GetSellingCategoriesUseCase(repo)
    }
    @Provides
    fun provideReturningCategoryUseCase(repo: AnalysisRepo): GetReturningCategoriesUseCase {
        return GetReturningCategoriesUseCase(repo)
    }
    @Provides
    fun provideTheHighestProfitProductsUseCase(repo: AnalysisRepo): GetTheHighestProfitProductsUseCase {
        return GetTheHighestProfitProductsUseCase(repo)
    }
    @Provides
    fun provideHaveNotSoldProductsUseCase(repo: AnalysisRepo): GetHaveNotSoldProductsUseCase {
        return GetHaveNotSoldProductsUseCase(repo)
    }

    @Provides
    fun provideGetTheLeastSellingCategoryUseCase(repo: AnalysisRepo): GetTheLeastSellingCategoryUseCase {
        return GetTheLeastSellingCategoryUseCase(repo)
    }
    @Provides
    fun provideGetTheMostSellingCategoryUseCase(repo: AnalysisRepo): GetTheMostSellingCategoryUseCase {
        return GetTheMostSellingCategoryUseCase(repo)
    }





}