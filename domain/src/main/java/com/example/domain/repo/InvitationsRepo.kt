package com.example.domain.repo

import com.example.domain.model.remoteModels.Invitation

interface InvitationsRepo {
    suspend fun createInvitation(email:String,code:String): Pair<Boolean, String>
    suspend fun deleteInvite(invite: Invitation): Pair<Boolean, String>
    suspend fun getAllInvitesForStore(): Pair<List<Invitation>, String>
    suspend fun getAllEmailPendingInvites(): Pair<List<Invitation>, String>
    suspend fun acceptInvite(invite: Invitation, code: String): Pair<Boolean, String>
    suspend fun rejectInvite(invite: Invitation): Pair<Boolean, String>
    fun sendEmail(code:String): Pair<String,String>
}