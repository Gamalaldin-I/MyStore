package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class LoginUseCase(private val authRepo: AuthRepo) {
    operator fun invoke(email: String, password: String, onResult: (Boolean, String) -> Unit){
        authRepo.login(email,password,onResult)
    }
}