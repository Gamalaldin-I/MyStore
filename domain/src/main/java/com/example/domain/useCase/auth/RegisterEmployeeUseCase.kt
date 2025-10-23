package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class RegisterEmployeeUseCase(private val authRepo: AuthRepo) {
    operator fun invoke(name: String, email:String,
                        password:String,
                        onResult:(success:Boolean,msg:String)->Unit){
        authRepo.registerEmployee(
            name,
            email,
            password,
            onResult
        )
    }
}