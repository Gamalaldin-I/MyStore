package com.example.domain.useCase.staff

import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo

class AcceptInviteUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(invite: Invite, code: String,  onResult: (Boolean, String) -> Unit) {
        staffRepo.acceptInvite(invite,code,onResult)
    }
}