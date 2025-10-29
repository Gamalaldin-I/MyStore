package com.example.domain.useCase.auth

import com.example.domain.repo.AuthRepo

class UpdateNameUseCase(private val repo: AuthRepo){
    operator fun invoke(name:String,onResult: (Boolean, String) -> Unit){
        repo.updateName(name,onResult)
    }
}