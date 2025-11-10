package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class RegisterUseCase(private val authRepo: AuthRepo) {
    operator fun invoke(email:String, password:String, name:String, role:Int,
                        onResult:(success:Boolean, msg:String)->Unit){
        authRepo.registerOwner(
            email,
            password,
            name,
            role,
            onResult
        )
    }
}