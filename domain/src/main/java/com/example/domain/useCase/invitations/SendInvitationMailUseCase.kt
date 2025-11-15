package com.example.domain.useCase.invitations

import com.example.domain.repo.InvitationsRepo

class SendInvitationMailUseCase(private val repo: InvitationsRepo) {
    operator fun invoke(
        code: String
    ): Pair<String, String>{
        return repo.sendEmail(code)
    }

}