package com.example.htopstore.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.auth.LoginUseCase
import com.example.domain.useCase.auth.SignWithGoogleUseCase
import com.example.domain.useCase.profile.ResetPasswordUseCase
import com.example.domain.util.Constants.SIGNUP_FIRST_ERROR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val loginWithGoogleUseCase: SignWithGoogleUseCase
):ViewModel(){
    private val _msg = MutableLiveData<String>()
    val msg : LiveData<String> = _msg
    fun login(email:String,password:String,onResult:(Boolean,String)->Unit){
        if(email.isEmpty()||password.isEmpty()){
            _msg.value = "Please fill all fields"
            return
        }
        loginUseCase(email,password){ success, msg ->
            onResult(success,msg)
        }
    }
    fun resetPassword(email: String,onRes:()->Unit){
        if(email.isEmpty()){
            _msg.value = "Please enter your email"
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
        loginWithGoogleUseCase(token=idToken,role = -1,storePhone = "",storeName = "",storeLocation = ""){ success, msg ->
            _msg.value = msg
            if(success){
                onRes()
            }
            if(msg==SIGNUP_FIRST_ERROR){
                goToSign()
            }
        }
    }


}