package com.example.domain.useCase.staff

import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo

class RejectInviteUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(invite: Invite, onResult: (Boolean, String) -> Unit) {
        staffRepo.rejectInvite(invite, onResult)
    }
}