package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class RegisterOwnerUseCase(private val authRepo: AuthRepo) {
    operator fun invoke(email:String, password:String, name:String, storeName: String,
                        storeLocation: String, storePhone: String,
                        onResult:(success:Boolean, msg:String)->Unit){
        authRepo.registerOwner(
            email,
            password,
            name,
            storeName,
            storeLocation,
            storePhone,
            onResult
        )
    }
}