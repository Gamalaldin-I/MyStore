package com.example.htopstore.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.roomDb.AppDataBase
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.profile.ChangeProfileImageUseCase
import com.example.domain.useCase.profile.DeleteAccountUseCase
import com.example.domain.useCase.profile.ObserveRoleChangingUseCase
import com.example.domain.useCase.profile.RemoveProfileImageUseCase
import com.example.domain.useCase.profile.UpdateNameUseCase
import com.example.domain.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject constructor(
        private val db: AppDataBase,
        private val logoutUseCase: LogoutUseCase,
        private val pref: SharedPref,
        private val changeProfileImageUseCase:ChangeProfileImageUseCase,
        private val removeProfileImageUseCase: RemoveProfileImageUseCase,
        private val updateNameUseCase: UpdateNameUseCase,
        private val deleteAccount: DeleteAccountUseCase,
        private val observeRoleChangingUseCase: ObserveRoleChangingUseCase
        ): ViewModel() {
            val role = pref.getRole()
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun updateName(name: String, onView: () -> Unit) {
        viewModelScope.launch{
            val  (success, msg) = withContext(Dispatchers.IO){updateNameUseCase(name)}
            if(success) onView()
            _message.postValue(msg)
        }
    }
    fun logout(context: Context,onResult: (Boolean, String) -> Unit){
        try {
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
                //clear cash
                val cashDir = context.cacheDir
                cashDir.deleteRecursively()
                withContext(Dispatchers.IO){db.clearAllTables()}
            }
            onResult(success, msg)
            _message.postValue(msg)
        }}catch (_: Exception){
            onResult(false, "Something went wrong")
        }
    }
    fun deleteYourAccount(context: Context,onResult: (Boolean, String) -> Unit){
        try {
            viewModelScope.launch{
                val (success, msg) = deleteAccount()
                if(success){
                    pref.clearPrefs()
                    //clear cash
                    val cashDir = context.cacheDir
                    cashDir.deleteRecursively()
                    withContext(Dispatchers.IO){db.clearAllTables()}
                }
                onResult(success, msg)
                _message.postValue(msg)
            }}catch (_: Exception){
            onResult(false, "Something went wrong")
        }
    }
    fun getUserData():User{
        return pref.getUser()
    }
    fun getStoreData():Store{
        return pref.getStore()
    }
    fun getRole():String? {
        val role = pref.getRole()
        return Constants.getRoleName(role)
    }


    fun isLoginFromGoogle(): Boolean{
        return pref.isLoginFromGoogle()
    }
    fun getProfileImage():String{
        return pref.getProfileImage().toString()
    }
    fun changePhoto(uri:Uri){
        viewModelScope.launch(Dispatchers.IO){
            changeProfileImageUseCase(uri){
                    success, msg ->
                _message.postValue(msg)
            }
        }
    }
    fun removeProfilePhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            removeProfileImageUseCase { success, msg ->
                _message.postValue(msg)
            }
        }
    }
    fun observerRole(onPromoted: (Int)->Unit){
        viewModelScope.launch(Dispatchers.IO){
            val role = observeRoleChangingUseCase.observeRole()
            if(role != -1 && role != pref.getRole()){
                pref.setRole(role)
                if(role>pref.getRole()) {
                    withContext(Dispatchers.Main){
                        onPromoted(0)
                    }
                }else{
                    withContext(Dispatchers.Main){
                        onPromoted(1)
                    }
                }
            }else{
                withContext(Dispatchers.Main){
                    onPromoted(-1)
                }
            }
        }
    }

}