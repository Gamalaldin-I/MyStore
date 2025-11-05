package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class ResetPasswordUseCase(private val repo: ProfileRepo) {
    suspend operator fun invoke(email: String, onResult: (Boolean, String) -> Unit){
        repo.resetPassword(email, onResult)
    }
}