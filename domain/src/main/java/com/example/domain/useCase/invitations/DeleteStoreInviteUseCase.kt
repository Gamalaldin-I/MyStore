package com.example.domain.useCase.invitations

import com.example.domain.model.remoteModels.Invitation
import com.example.domain.repo.InvitationsRepo

class DeleteStoreInviteUseCase(private val repo: InvitationsRepo){
    suspend operator fun invoke(invite: Invitation):Pair<Boolean, String> {
        return repo.deleteInvite(invite)
    }
}