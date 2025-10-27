package com.example.htopstore.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.useCase.auth.LoginUseCase
import com.example.domain.useCase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
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
        resetPasswordUseCase(email) { success, msg ->
            _msg.value = msg
            if(success){
                onRes()
            }
        }

    }


}