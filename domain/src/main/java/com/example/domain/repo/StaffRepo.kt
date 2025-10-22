package com.example.domain.repo

import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import kotlinx.coroutines.flow.StateFlow

interface StaffRepo {
    val invitesFlow:StateFlow<List<Invite>>
    val employeesFlow:StateFlow<List<StoreEmployee>>

    fun listenToInvites()
    fun addInvite(email:String,code: String,onResult:(success: Boolean,msg:String)->Unit)
    fun deleteInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit)
    fun listenToEmployees()
    fun stopListening()
}