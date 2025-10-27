package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class ChangePasswordUseCase(private val repo: AuthRepo) {
    operator fun invoke (oldPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit){
        repo.changePassword(oldPassword,newPassword,onResult)

    }
}