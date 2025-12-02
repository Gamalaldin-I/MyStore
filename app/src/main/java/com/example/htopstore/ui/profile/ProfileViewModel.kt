package com.example.htopstore.ui.profile

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
        private val updateNameUseCase: UpdateNameUseCase
        ): ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun updateName(name: String, onView: () -> Unit) {
        viewModelScope.launch{
            val  (success, msg) = withContext(Dispatchers.IO){updateNameUseCase(name)}
            if(success) onView()
            _message.postValue(msg)
        }
    }
    fun logout(onResult: (Boolean, String) -> Unit){
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
                withContext(Dispatchers.IO){db.clearAllTables()}
            }
            onResult(success, msg)
            _message.postValue(msg)
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

}