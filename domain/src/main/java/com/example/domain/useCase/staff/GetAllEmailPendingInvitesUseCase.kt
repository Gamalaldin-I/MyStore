package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class GetAllEmailPendingInvitesUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(onResult: (Boolean, String) -> Unit) {
        staffRepo.getAllInvitesForEmployee(onResult)
    }
}