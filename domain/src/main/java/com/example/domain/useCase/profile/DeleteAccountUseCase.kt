package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class DeleteAccountUseCase(private val repo: ProfileRepo){
    suspend operator fun invoke():Pair<Boolean,String> = repo.deleteAccount()

}