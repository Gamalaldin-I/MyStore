package com.example.htopstore.di

import android.content.Context
import com.example.htopstore.data.local.repo.home.HomeRepo
import com.example.htopstore.data.local.repo.home.HomeRepoImp
import com.example.htopstore.domain.useCase.home.GetLowStockUseCase
import com.example.htopstore.domain.useCase.home.GetTodayBriefUseCase
import com.example.htopstore.domain.useCase.home.GetTop5InSalesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object HomeModule {
    @Provides
    fun provideHomeRepo( @ApplicationContext context: Context): HomeRepoImp {
        return HomeRepoImp(context)
    }
    @Provides
    fun provideGetLowStockUseCase(homeRepo: HomeRepoImp): GetLowStockUseCase {
        return GetLowStockUseCase(homeRepo)
    }
    @Provides
    fun provideGetTop5InSalesUseCase(homeRepo: HomeRepoImp): GetTop5InSalesUseCase {
        return GetTop5InSalesUseCase(homeRepo)
    }
    @Provides
    fun provideGetTodayBriefUseCase(homeRepo: HomeRepoImp): GetTodayBriefUseCase {
        return GetTodayBriefUseCase(homeRepo)
    }

}