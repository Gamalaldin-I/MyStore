package com.example.htopstore.ui.changeEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.profile.ResetPasswordUseCase
import com.example.domain.useCase.profile.UpdateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val pref: SharedPref,
    private val changeEmailUseCase: UpdateEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
): ViewModel() {
    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg
    fun changeEmail(newEmail: String, password: String,onFail:()->Unit,onSuccess:()->Unit){
        viewModelScope.launch {
        changeEmailUseCase(newEmail, password){
            success, msg ->
            if (success){ onSuccess() }
            else{ onFail()
                _msg.value = msg
            }
        }}
    }

    fun resetPassword(){
        viewModelScope.launch {
            resetPasswordUseCase(pref.getUser().email) { success, msg ->
                _msg.postValue(msg)
            }
        }
    }


    fun getOldEmail():String{
      return  pref.getUser().email
    }
    fun logout(){
        pref.clearPrefs()
    }
}