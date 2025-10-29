package com.example.htopstore.di.module

import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.repo.AuthRepo
import com.example.domain.useCase.auth.ChangePasswordUseCase
import com.example.domain.useCase.auth.LoginUseCase
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.auth.RegisterEmployeeUseCase
import com.example.domain.useCase.auth.RegisterOwnerUseCase
import com.example.domain.useCase.auth.ResetPasswordUseCase
import com.example.domain.useCase.auth.UpdateEmailUseCase
import com.example.domain.useCase.auth.UpdateNameUseCase
import com.example.domain.useCase.auth.UpdateStoreDataUseCase
import com.example.htopstore.util.firebase.auth.AuthRepoImp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideAuthRepo(db: FirebaseFirestore,pref: SharedPref): AuthRepo {
        return AuthRepoImp(db,pref)
    }

    @Provides
    fun provideLoginUseCase(authRepo: AuthRepo): LoginUseCase {
        return LoginUseCase(authRepo)}

    @Provides
    fun provideRegisterOwnerUseCase(authRepo: AuthRepo): RegisterOwnerUseCase {
        return RegisterOwnerUseCase(authRepo)}

    @Provides
    fun provideRegisterEmployeeUseCase(authRepo: AuthRepo): RegisterEmployeeUseCase {
        return RegisterEmployeeUseCase(authRepo)}

    @Provides
    fun provideLogoutUseCase(authRepo: AuthRepo): LogoutUseCase {
        return LogoutUseCase(authRepo)
    }

    @Provides
    fun provideResetPasswordUseCase(authRepo: AuthRepo): ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepo)
    }

    @Provides
        fun provideChangePasswordUseCase(authRepo: AuthRepo): ChangePasswordUseCase {
            return ChangePasswordUseCase(authRepo)
    }
    @Provides
    fun provideChangeNameUseCase(repo: AuthRepo): UpdateNameUseCase{
        return UpdateNameUseCase(repo)
    }
    @Provides
    fun provideUpdateEmailUseCase(repo: AuthRepo): UpdateEmailUseCase {
        return UpdateEmailUseCase(repo)
    }
    @Provides
    fun provideUpdateStoreUseCase(repo: AuthRepo): UpdateStoreDataUseCase{
        return UpdateStoreDataUseCase(repo)
    }
}