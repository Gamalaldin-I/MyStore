package com.example.htopstore.di.module

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.AuthRepoImp
import com.example.domain.repo.AuthRepo
import com.example.domain.useCase.auth.LoginUseCase
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.auth.RegisterUseCase
import com.example.domain.useCase.auth.SignWithGoogleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideAuthRepo(db: SupabaseClient, pref: SharedPref,networkHelper: NetworkHelperInterface): AuthRepo {
        return AuthRepoImp(
            supabase = db,
            sharedPref = pref,
            networkHelper = networkHelper
        )
    }

    @Provides
    fun provideLoginUseCase(authRepo: AuthRepo): LoginUseCase {
        return LoginUseCase(authRepo)}

    @Provides
    fun provideRegisterOwnerUseCase(authRepo: AuthRepo): RegisterUseCase {
        return RegisterUseCase(authRepo)}


    @Provides
    fun provideLogoutUseCase(authRepo: AuthRepo): LogoutUseCase {
        return LogoutUseCase(authRepo)
    }

    @Provides
    fun provideSignWithGoogle(repo: AuthRepo): SignWithGoogleUseCase{
        return SignWithGoogleUseCase(repo)
    }

}