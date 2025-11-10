package com.example.domain.useCase.staff

import android.content.Context
import com.example.domain.repo.StaffRepo

class SendInvitationMailUseCase(private val repo: StaffRepo,private val context: Context) {
    operator fun invoke(
        recipientEmail: String,
        code: String
    ){
        repo.sendEmail(context,recipientEmail,code)
    }

}