package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class UpdateEmailUseCase(private val repo: ProfileRepo) {
    suspend operator fun invoke(newEmail: String, password: String,
                        onResult: (Boolean, String) -> Unit){
        repo.updateEmail(newEmail, password, onResult)
    }
}