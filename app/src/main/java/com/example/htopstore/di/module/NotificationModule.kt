package com.example.htopstore.di.module

import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.NotificationsRepoImp
import com.example.domain.repo.NotificationsRepo
import com.example.domain.useCase.notifications.GetNotificationsUseCase
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    @Provides
    @Singleton
    fun provideNotRepo(
        supabaseClient: SupabaseClient,
        networkHelperInterface: NetworkHelperInterface

    ): NotificationsRepo{
        return NotificationsRepoImp(supabaseClient,networkHelperInterface)
    }
    @Provides
    @Singleton
    fun providesGetNotifications(repo: NotificationsRepo): GetNotificationsUseCase{
        return GetNotificationsUseCase(repo)
    }
    @Provides
    @Singleton
    fun providesInsertNotifications(repo: NotificationsRepo): InsertNotificationUseCase{
        return InsertNotificationUseCase(repo)
    }

}