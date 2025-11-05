package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class RemoveProfileImageUseCase(private val repo: ProfileRepo) {
    suspend operator fun invoke(onResult: (Boolean, String) -> Unit){
        repo.removeProfileImage(onResult)
    }
}