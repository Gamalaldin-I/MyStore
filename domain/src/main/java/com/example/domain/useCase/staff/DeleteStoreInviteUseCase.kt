package com.example.domain.useCase.staff

import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo

class DeleteStoreInviteUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(i:Invite,onResult:(success: Boolean,msg:String)->Unit) {
        return staffRepo.deleteInvite(i,onResult)
    }
}