package com.example.htopstore.di.module.store

import android.content.Context
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.ProfileRepoImp
import com.example.domain.repo.ProfileRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.useCase.profile.ChangeProfileImageUseCase
import com.example.domain.useCase.profile.ObserveRoleChangingUseCase
import com.example.domain.useCase.profile.RemoveProfileImageUseCase
import com.example.domain.useCase.profile.ResetPasswordUseCase
import com.example.domain.useCase.profile.UpdateEmailUseCase
import com.example.domain.useCase.profile.UpdateNameUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {
    @Provides
    fun provideProfileRepo(
        supa: SupabaseClient,
        pref: SharedPref,
        @ApplicationContext context: Context,
        networkHelperInterface: NetworkHelperInterface,
        insertNotificationUseCase: InsertNotificationUseCase

    ): ProfileRepo = ProfileRepoImp(
        supabase = supa,
        pref = pref,
        context = context,
        networkHelperInterface,
        notificationUseCase =insertNotificationUseCase
    )

    @Provides
    fun provideChangeProfilePhoto(repo: ProfileRepo) = ChangeProfileImageUseCase(repo)

    @Provides
    fun provideRemoveProfilePhoto(repo: ProfileRepo) = RemoveProfileImageUseCase(repo)

    @Provides
    fun provideResetPasswordUseCase(repo: ProfileRepo): ResetPasswordUseCase {
        return ResetPasswordUseCase(repo)
    }

    @Provides
    fun provideChangeNameUseCase(repo: ProfileRepo): UpdateNameUseCase {
        return UpdateNameUseCase(repo)
    }
    @Provides
    fun provideUpdateEmailUseCase(repo: ProfileRepo): UpdateEmailUseCase {
        return UpdateEmailUseCase(repo)
    }
    @Provides
    fun provideObserveRoleUseCase(repo: ProfileRepo): ObserveRoleChangingUseCase {
        return ObserveRoleChangingUseCase(repo)
    }
}