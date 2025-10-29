package com.example.htopstore.ui.changeEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.UpdateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val pref: SharedPref,
    private val changeEmailUseCase: UpdateEmailUseCase
): ViewModel() {
    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg
    fun changeEmail(newEmail: String, password: String,onFail:()->Unit,onSuccess:()->Unit){
        changeEmailUseCase(newEmail, password){
            success, msg ->
            if (success){ onSuccess() }
            else{ onFail()
                _msg.value = msg
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