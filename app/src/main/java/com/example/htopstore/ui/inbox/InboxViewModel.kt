package com.example.htopstore.ui.inbox

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.staff.AcceptInviteUseCase
import com.example.domain.useCase.staff.GetAllEmailPendingInvitesUseCase
import com.example.domain.useCase.staff.RejectInviteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getAllEmailPendingInvitesUseCase: GetAllEmailPendingInvitesUseCase,
    private val acceptInviteUseCase: AcceptInviteUseCase,
    private val rejectInviteUseCase: RejectInviteUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val staffRepo: StaffRepo,
    private val pref: SharedPref
): ViewModel() {

    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    val invites = staffRepo.invitesFlow

    fun getAllInvites(){
        getAllEmailPendingInvitesUseCase{
            success,msg->
            _msg.value = msg
    }
    }
    fun getEmail(): String{
        Log.d("InboxViewModel", "login: ${pref.isLogin()}  store id ${pref.getStore().id}" +
                "  store name ${pref.getStore().name}" +
                " store phone ${pref.getStore().phone}" +
                "user id ${pref.getUser().id}" +
                "user name ${pref.getUser().name}")
        return pref.getUser().email
    }

    fun logout(onResult: (Boolean, String) -> Unit){
        pref.clearPrefs()
        logoutUseCase(onResult)
    }

    fun reject(invite: Invite,onAction:()->Unit) {
        rejectInviteUseCase(invite){
            success,msg->
            if(success){
                onAction()
            }
            _msg.value = msg
        }
    }


    fun accept(invite: Invite,code:String,onAction:()->Unit)
    {
        acceptInviteUseCase(invite,code){
            success,msg->
            if(success){
                onAction()
            }
            _msg.value = msg
        }

    }
    fun validToGoHome(goToMain:()->Unit){
        if(pref.isLogin()&& pref.getStore().id.isNotEmpty()){
            goToMain()
        }
    }


    override fun onCleared() {
        super.onCleared()
        staffRepo.stopListening()
    }
}