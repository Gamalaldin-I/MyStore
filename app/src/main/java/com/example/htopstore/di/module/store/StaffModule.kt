package com.example.htopstore.di.module.store

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.repo.StaffRepoImp
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.staff.GetStoreEmployeesUseCase
import com.example.domain.useCase.staff.RejectOrRehireUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaffModule {
    @Singleton
    @Provides
    fun provideStaffRepo(
        supabaseClient: SupabaseClient,
        pref: SharedPref
    ): StaffRepo {
        return StaffRepoImp(supabase = supabaseClient, pref = pref)
    }
    @Provides
    fun provideEmployeesUseCase(staffRepo: StaffRepo): GetStoreEmployeesUseCase {
        return GetStoreEmployeesUseCase(staffRepo)
    }

    @Provides
    fun provideRejectOrRehireUseCase(staffRepo: StaffRepo): RejectOrRehireUseCase {
        return RejectOrRehireUseCase(staffRepo)
    }




}