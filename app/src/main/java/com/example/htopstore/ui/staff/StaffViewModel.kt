package com.example.htopstore.ui.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.staff.AddStoreInviteUseCase
import com.example.domain.useCase.staff.DeleteStoreInviteUseCase
import com.example.domain.useCase.staff.GetStoreEmployeesUseCase
import com.example.domain.useCase.staff.GetStoreInvitesUseCase
import com.example.domain.useCase.staff.RejectOrRehireUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val getStoreEmployeesUseCase: GetStoreEmployeesUseCase,
    private val getStoreInvitesUseCase: GetStoreInvitesUseCase,
    private val deleteStoreInviteUseCase: DeleteStoreInviteUseCase,
    private val addStoreInviteUseCase: AddStoreInviteUseCase,
    private val rejectOrRehireUseCase: RejectOrRehireUseCase,
    private val staffRepo: StaffRepo
): ViewModel(){
    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    val invites = staffRepo.invitesFlow
    val employees = staffRepo.employeesFlow

    fun getInvites(){
        getStoreInvitesUseCase()
    }
    fun hireOrFire(empId:String,hire:Boolean){
        rejectOrRehireUseCase(empId,hire){
            success,msg->
            _msg.value = msg
        }

    }

    fun addInvite(email: String, code:String,successAction:()->Unit){
        addStoreInviteUseCase(email, code){
            success,msg->
            if (success){
                successAction()
            }
            _msg.value = msg
        }

    }
    fun deleteInvite(invite: Invite,successAction:()->Unit){
        deleteStoreInviteUseCase(invite) { success, msg ->
            if (success) {
                successAction()
            }
            _msg.value = msg
        }
    }
    fun getEmployees() {
        getStoreEmployeesUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        staffRepo.stopListening()
    }

}