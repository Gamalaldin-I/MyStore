package com.example.domain.useCase.invitations

import com.example.domain.model.remoteModels.Invitation
import com.example.domain.repo.InvitationsRepo

class AcceptInviteUseCase(private val repo: InvitationsRepo){
    suspend operator fun invoke(invite: Invitation, code: String):Pair<Boolean, String> {
        return repo.acceptInvite(invite,code)
    }
}