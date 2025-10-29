package com.example.domain.useCase.auth

import com.example.domain.repo.StaffRepo

class UpdateStoreDataUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(name: String, phone: String, location: String, onResult: (Boolean, String) -> Unit){
        staffRepo.updateStore(name, phone, location, onResult)
    }
}