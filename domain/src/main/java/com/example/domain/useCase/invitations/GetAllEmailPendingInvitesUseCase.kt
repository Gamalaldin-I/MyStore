package com.example.domain.useCase.invitations

import com.example.domain.model.remoteModels.Invitation
import com.example.domain.repo.InvitationsRepo

class GetAllEmailPendingInvitesUseCase(private val repo: InvitationsRepo){
    suspend operator fun invoke():Pair<List<Invitation>, String> {
        return repo.getAllEmailPendingInvites()
    }
}