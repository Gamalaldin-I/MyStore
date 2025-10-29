package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class UpdateStoreDataUseCase(private val authRepo: AuthRepo){
    operator fun invoke(name: String, phone: String, location: String, onResult: (Boolean, String) -> Unit){
        authRepo.updateStoreData(name, phone, location, onResult)
    }
}