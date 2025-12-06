package com.example.domain.useCase.profile

import com.example.domain.repo.ProfileRepo

class ObserveRoleChangingUseCase(private val repo: ProfileRepo){
    suspend fun observeRole() = repo.observeRole()
}