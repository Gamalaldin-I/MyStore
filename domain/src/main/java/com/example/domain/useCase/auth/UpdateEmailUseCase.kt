package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class UpdateEmailUseCase(private val repo: AuthRepo) {
    operator fun invoke(newEmail: String, password: String,
                        onResult: (Boolean, String) -> Unit){
        repo.updateEmail(newEmail, password, onResult)
    }
}