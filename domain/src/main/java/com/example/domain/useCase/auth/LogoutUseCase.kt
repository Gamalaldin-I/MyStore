package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class LogoutUseCase(private val authRepo: AuthRepo) {
    operator fun invoke(onResult:(success:Boolean,msg:String)->Unit){
        authRepo.logout(onResult)
    }
}