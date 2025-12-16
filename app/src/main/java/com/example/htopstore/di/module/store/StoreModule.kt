package com.example.htopstore.di.module.store

import android.content.Context
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.StoreRepoImp
import com.example.domain.repo.StoreRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.useCase.store.AddCategoryUseCase
import com.example.domain.useCase.store.AddStoreUseCase
import com.example.domain.useCase.store.DeleteCategoryUseCase
import com.example.domain.useCase.store.DeleteStoreUseCase
import com.example.domain.useCase.store.UpdateStoreDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient

@Module
@InstallIn(SingletonComponent::class)
object StoreModule {
    @Provides
    fun provideAddStoreUseCase(repo: StoreRepo): AddStoreUseCase {
        return AddStoreUseCase(repo)
    }
    @Provides
    fun provideStoreRepo(
        @ApplicationContext context: Context,
        supabase: SupabaseClient,
        pref: SharedPref,
        networkHelper: NetworkHelperInterface,
        insertNotificationUseCase: InsertNotificationUseCase
    ): StoreRepo {
        return StoreRepoImp(
            supabase = supabase,
            pref = pref,
            context = context,
            networkHelper = networkHelper,
            notSender = insertNotificationUseCase
        )
    }
    @Provides
    fun provideUpdateStoreUseCase(repo: StoreRepo): UpdateStoreDataUseCase {
        return UpdateStoreDataUseCase(repo)
    }
    @Provides
    fun provideDeleteCategoryUseCase(repo: StoreRepo): DeleteCategoryUseCase {
        return DeleteCategoryUseCase(repo)
    }
    @Provides
    fun provideAddCategoryUseCase(repo: StoreRepo): AddCategoryUseCase {
        return AddCategoryUseCase(repo)
    }
    @Provides
    fun provideDeleteStoreUseCase(repo: StoreRepo): DeleteStoreUseCase {
        return DeleteStoreUseCase(repo)
    }

}