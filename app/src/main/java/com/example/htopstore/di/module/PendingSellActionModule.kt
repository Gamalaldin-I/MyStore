package com.example.htopstore.di.module

import com.example.domain.repo.SalesRepo
import com.example.domain.useCase.pendingSellActions.AddSellPendingActionUseCase
import com.example.domain.useCase.pendingSellActions.DeleteApprovedSellActionsUseCase
import com.example.domain.useCase.pendingSellActions.DeletePendingActionByIdUseCase
import com.example.domain.useCase.pendingSellActions.GetAllSellActionsUseCase
import com.example.domain.useCase.pendingSellActions.GetSellPendingActionByIdUseCase
import com.example.domain.useCase.pendingSellActions.UpdateSellActionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PendingSellActionModule {
    @Provides
    @Singleton
    fun provideAddingPendingSellActionUseCase(repo: SalesRepo): AddSellPendingActionUseCase{
        return AddSellPendingActionUseCase(repo)
    }
    @Provides
    @Singleton
    fun provideUpdatePendingSellActionUseCase(repo: SalesRepo): UpdateSellActionUseCase{
        return UpdateSellActionUseCase(repo)
    }
    @Provides
    @Singleton
    fun provideDeleteApprovedSellActionUseCase(repo: SalesRepo): DeleteApprovedSellActionsUseCase{
        return DeleteApprovedSellActionsUseCase(repo)
    }
    @Provides
    @Singleton
    fun provideGettingPendingSellActionUseCase(repo: SalesRepo): GetAllSellActionsUseCase{
        return GetAllSellActionsUseCase(repo)
    }
    @Provides
    @Singleton
    fun provideDeletePendingActionByIdUseCase(repo: SalesRepo): DeletePendingActionByIdUseCase{
        return DeletePendingActionByIdUseCase(repo)
    }
    @Provides
    @Singleton
    fun provideGetPendingActionByIdUseCase(repo: SalesRepo): GetSellPendingActionByIdUseCase{
        return GetSellPendingActionByIdUseCase(repo)
    }

}