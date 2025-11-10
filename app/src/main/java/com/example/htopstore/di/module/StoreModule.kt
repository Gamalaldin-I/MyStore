package com.example.htopstore.di.module

import com.example.domain.repo.StoreRepo
import com.example.domain.useCase.store.AddStoreUseCase
import com.example.domain.useCase.store.UpdateStoreDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object StoreModule {
    @Provides
    fun provideAddStoreUseCase(repo: StoreRepo): AddStoreUseCase{
        return AddStoreUseCase(repo)
    }
    @Provides
    fun provideUpdateStoreUseCase(repo: StoreRepo): UpdateStoreDataUseCase{
        return UpdateStoreDataUseCase(repo)
    }
}