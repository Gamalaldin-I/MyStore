package com.example.data.remote.repo

import android.content.Context
import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.repo.StaffRepo
import kotlinx.coroutines.flow.StateFlow

class StaffRepoImp(
) : StaffRepo {

    override lateinit var invitesFlow: StateFlow<List<Invite>>
    override lateinit var employeesFlow: StateFlow<List<StoreEmployee>>
    override lateinit var employeeStatus: StateFlow<String>
    override fun listenToInvites() {
        TODO("Not yet implemented")
    }

    override fun addInvite(
        email: String,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteInvite(
        invite: Invite,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun listenToEmployees() {
        TODO("Not yet implemented")
    }

    override fun stopListening() {
        TODO("Not yet implemented")
    }

    override fun getAllInvitesForEmployee(onResult: (Boolean, String) -> Unit) {
        onResult(true, "Success")
    }

    override fun acceptInvite(
        invite: Invite,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectInvite(
        invite: Invite,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectOrRehireEmployee(
        employeeId: String,
        reject: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateStore(
        name: String,
        phone: String,
        location: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun sendEmail(
        context: Context,
        recipientEmail: String,
        code: String
    ): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun listenToEmployee() {
        TODO("Not yet implemented")
    }
}