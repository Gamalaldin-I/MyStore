package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class SignWithGoogleUseCase(private val authRepo: AuthRepo){
    operator fun invoke(token:String,
                        storePhone:String,
                        storeName:String,
                        storeLocation:String,
                        role:Int,
                        onResult:(success:Boolean,msg:String)->Unit){
        authRepo.signWithGoogle(token,
            role,
            storePhone,
            storeName,
            storeLocation,
            onResult)
    }
}