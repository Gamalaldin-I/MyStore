package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class UpdateNameUseCase(private val repo: ProfileRepo){
    suspend operator fun invoke(name:String):Pair<Boolean,String>{
        return repo.updateName(name)
    }
}