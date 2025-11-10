package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class SignWithGoogleUseCase(private val authRepo: AuthRepo){
    suspend operator fun invoke(token:String,
                                role:Int,
                                fromLoginScreen: Boolean):Pair<Boolean,String> =
        authRepo.signWithGoogle(
            token,
            role,
            fromLoginScreen)
    }
