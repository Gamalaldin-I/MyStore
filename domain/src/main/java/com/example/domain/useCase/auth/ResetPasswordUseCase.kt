package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class ResetPasswordUseCase(private val repo: AuthRepo) {
    operator fun invoke(email: String, onResult: (Boolean, String) -> Unit){
        repo.resetPassword(email, onResult)
    }
}