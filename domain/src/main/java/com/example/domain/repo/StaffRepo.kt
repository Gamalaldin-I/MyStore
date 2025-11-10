package com.example.domain.repo

import android.content.Context
import com.example.domain.model.remoteModels.Invite

interface StaffRepo {
    // for owner
    fun addInvite( email:String, code: String, onResult:(success: Boolean, msg:String)->Unit)
    fun deleteInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit)
    fun listenToEmployees()

    // for employee
    fun getAllInvitesForEmployee(onResult:(success:Boolean,msg:String)->Unit)
    fun acceptInvite(invite: Invite,code:String,onResult:(success: Boolean,msg:String)->Unit)
    fun rejectInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit)
    fun rejectOrRehireEmployee(employeeId:String,reject:Boolean,onResult:(success:Boolean,msg:String)->Unit)
    fun sendEmail(context: Context, recipientEmail:String, code:String): Pair<String,String>



}