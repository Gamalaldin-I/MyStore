package com.example.htopstore.ui.inbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.invitations.AcceptInviteUseCase
import com.example.domain.useCase.invitations.GetAllEmailPendingInvitesUseCase
import com.example.domain.useCase.invitations.RejectInviteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getAllEmailPendingInvitesUseCase: GetAllEmailPendingInvitesUseCase,
    private val acceptInviteUseCase: AcceptInviteUseCase,
    private val rejectInviteUseCase: RejectInviteUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val pref: SharedPref
): ViewModel() {

    private val _invites = MutableLiveData<List<Invitation>>()
    val invites: LiveData<List<Invitation>> = _invites


    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg


    ////////////////////////////////////////////////////
    /////////////Get ALL PENDING INVITATIONS///////////
    //////////////////////////////////////////////////
    fun getAllPendingInvitations(){
        viewModelScope.launch(Dispatchers.IO) {
            val (invites, msg) = getAllEmailPendingInvitesUseCase()
            _invites.postValue(invites)
            _msg.postValue("$msg  size ${invites.size}")
        }
    }


    fun getEmail(): String{
        return pref.getUser().email
    }



    ////////////////////////////////////////////////////
    /////////////////////////////LOGOUT////////////////
    //////////////////////////////////////////////////

    fun logout(onResult: (Boolean, String) -> Unit){
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
            }
            onResult(success, msg)
        }
    }

    ////////////////////////////////////////////////////
    /////////////REJECT INVITATION///////////
    //////////////////////////////////////////////////

    fun reject(invite: Invitation, onAction:()->Unit) {
        viewModelScope.launch(Dispatchers.IO){
            val (success, msg) = rejectInviteUseCase(invite)
            if(success){
                withContext(Dispatchers.Main){ onAction()
            }
            }
            _msg.postValue(msg)
        }

    }

    ////////////////////////////////////////////////////
    /////////////ACCEPT  INVITATION////////////////////
    //////////////////////////////////////////////////

    fun accept(invite: Invitation, code:String, onAction:()->Unit) {
        viewModelScope.launch(Dispatchers.IO){
            val (success, msg) = acceptInviteUseCase(invite, code)
            if(success){
                withContext(Dispatchers.Main){ onAction()
                }
            }
            _msg.postValue(msg)
        }
    }
    fun validToGoHome(goToMain:()->Unit){
        if(pref.isLogin()&& pref.getStore().id.isNotEmpty()){
            goToMain()
        }
    }

}