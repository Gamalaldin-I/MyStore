package com.example.htopstore.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.auth.LoginUseCase
import com.example.domain.useCase.auth.SignWithGoogleUseCase
import com.example.domain.useCase.profile.ResetPasswordUseCase
import com.example.domain.util.Constants.SIGNUP_FIRST_ERROR
import com.example.htopstore.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val loginWithGoogleUseCase: SignWithGoogleUseCase,
    private val app:Application
): AndroidViewModel(app){
    private val _msg = MutableLiveData<String>()
    val msg : LiveData<String> = _msg
    fun login(email:String,password:String,onResult:(Boolean,String)->Unit){
        if(email.isEmpty()||password.isEmpty()){
            _msg.value = app.getString(R.string.please_fill_all_fields)
            return
        }
        loginUseCase(email,password){ success, msg ->
            onResult(success,msg)
        }
    }
    fun resetPassword(email: String,onRes:()->Unit){
        if(email.isEmpty()){
            _msg.value = app.getString(R.string.enter_valid_email)
            return
        }
        viewModelScope.launch {
        resetPasswordUseCase(email) { success, msg ->
            _msg.postValue(msg)
            if(success){
                onRes()
            }
        }}
    }
    fun loginWithGoogle(idToken:String,goToSign:()->Unit,onRes:()->Unit){
        viewModelScope.launch {
            val (success,msg) = loginWithGoogleUseCase(token=idToken,role = -1,fromLoginScreen = true)
            _msg.postValue(msg)
            if(success) withContext(Dispatchers.Main){
                onRes()
            }
            if(msg==SIGNUP_FIRST_ERROR){
                withContext(Dispatchers.Main){goToSign()}
            }
        }
    }


}