package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class LogoutUseCase(private val authRepo: AuthRepo) {
    suspend operator  fun invoke(): Pair<Boolean,String> {
        return authRepo.logout()
    }
}