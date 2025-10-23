package com.example.domain.repo

import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import kotlinx.coroutines.flow.StateFlow

interface StaffRepo {
    val invitesFlow:StateFlow<List<Invite>>
    val employeesFlow:StateFlow<List<StoreEmployee>>
// for owner
    fun listenToInvites()
    fun addInvite(email:String,code: String,onResult:(success: Boolean,msg:String)->Unit)
    fun deleteInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit)
    fun listenToEmployees()
    fun stopListening()

    // for employee
    fun getAllInvitesForEmployee(onResult:(success:Boolean,msg:String)->Unit)
    fun acceptInvite(invite: Invite,code:String,onResult:(success: Boolean,msg:String)->Unit)
    fun rejectInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit)

}