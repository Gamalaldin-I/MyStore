package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class AddStoreInviteUseCase(private val staffRepo: StaffRepo){
    operator fun invoke(email:String,code:String,onResult:(success: Boolean,msg:String)->Unit) {
        return staffRepo.addInvite(email,code,onResult)
    }
}