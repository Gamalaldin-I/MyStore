package com.example.htopstore.ui.changeEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.profile.ResetPasswordUseCase
import com.example.domain.useCase.profile.UpdateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            val (success,msg) = changeEmailUseCase(newEmail, password)
            if(success) withContext(Dispatchers.Main) {onSuccess()}
            else withContext(Dispatchers.Main) {onFail()}
            _msg.postValue(msg)

        }
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