package com.example.domain.useCase.invitations

import com.example.domain.repo.InvitationsRepo

class AddStoreInviteUseCase(private val repo: InvitationsRepo){
    suspend operator fun invoke(email:String,code:String):Pair<Boolean, String> {
        val invite= return repo.createInvitation(email,code)
    }
}