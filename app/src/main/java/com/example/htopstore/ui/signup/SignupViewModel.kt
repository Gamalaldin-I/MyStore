package com.example.htopstore.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.RegisterEmployeeUseCase
import com.example.domain.useCase.auth.RegisterOwnerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val registerOwnerUseCase: RegisterOwnerUseCase,
    private val registerEmployeeUseCase: RegisterEmployeeUseCase
) : ViewModel(), OnNextStep {
    private var userName = ""
    private var email = ""
    private var password = ""
    private var code =""

    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    val isLogin: LiveData<Boolean> = MutableLiveData<Boolean>(sharedPref.isLogin())






    override fun afterRoleSelection(role: Int, nextAction: () -> Unit) {
        sharedPref.setRole(role)
        nextAction()
    }

    override fun afterUserFormFill(
        name: String,
        email: String,
        password: String,
        nextAction: () -> Unit
    ) {
        this.userName = name
        this.email = email
        this.password = password
        nextAction()
    }

    override fun afterStoreFormFill(
        name: String,
        location: String,
        phone: String,
        nextAction: () -> Unit
    ) {
        registerOwnerUseCase(
             email,
            password,
            userName,
            name,
            location,
            phone){
            success,msg->
            if (success){
                nextAction()
            }
            _msg.value = msg
        }
    }

    override fun afterSendCode(code: String, nextAction: () -> Unit) {
        this.code = code
        registerEmployeeUseCase(this.userName,
            this.email,
            this.password,
            this.code
            ){
            success,msg->
            if (success) {
                nextAction()
            }
            _msg.value = msg
        }
    }


    fun getRole(): Int {
        return sharedPref.getRole()
    }

    fun validUserData(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _msg.value = "Please fill all fields"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _msg.value = "Please enter a valid email"
            return false
        }
        if (password.length < 8) {
            _msg.value = "Password must be at least 8 characters"
            return false
        }
        if (password != confirmPassword) {
            _msg.value = "Passwords do not match"
            return false
        }
        return true
    }

    fun validStoreData(
        name: String,
        location: String,
        phone: String
    ): Boolean {
        if (name.isEmpty() || location.isEmpty() || phone.isEmpty()) {
            _msg.value = "Please fill all fields"
            return false
        }
        if (name.length < 3) {
            _msg.value = "Please enter a valid name"
            return false
        }
        if (phone.length != 11) {
            _msg.value = "Please enter a valid phone number"
            return false
        }
        if (!phone.startsWith("01")) {
            _msg.value = "Please enter a valid phone number"
            return false
        }
        return true
    }

    fun validCode(code: String): Boolean {
        if (code.isEmpty()) {
            _msg.value = "Please fill the field"
            return false
        }
        if (code.length != 6) {
            _msg.value = "Please enter a valid code"
            return false
        }
        _msg.value = "Sending"
        return true
    }
}