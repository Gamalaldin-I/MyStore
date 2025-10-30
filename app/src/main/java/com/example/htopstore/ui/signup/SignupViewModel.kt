package com.example.htopstore.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.RegisterEmployeeUseCase
import com.example.domain.useCase.auth.RegisterOwnerUseCase
import com.example.domain.useCase.auth.SignWithGoogleUseCase
import com.example.domain.util.Constants
import com.example.htopstore.util.DataValidator.validEmail
import com.example.htopstore.util.DataValidator.validPassword
import com.example.htopstore.util.DataValidator.validPasswordMatch
import com.example.htopstore.util.DataValidator.validPhone
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val registerOwnerUseCase: RegisterOwnerUseCase,
    private val registerEmployeeUseCase: RegisterEmployeeUseCase,
    private val signWithGoogleUseCase: SignWithGoogleUseCase
) : ViewModel(), OnNextStep {

    // User credentials
    private var userName: String = ""
    private var userEmail: String = ""
    private var userPassword: String = ""

    // Store information
    private var storeName: String = ""
    private var storeLocation: String = ""
    private var storePhone: String = ""

    // LiveData for UI feedback
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val isLoggedIn: LiveData<Boolean> = MutableLiveData(sharedPref.isLogin())


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
        userName = name
        userEmail = email
        userPassword = password

        when (getRole()) {
            Constants.OWNER_ROLE -> {
                // Owner needs to fill store info first before registration
                registerOwner(nextAction)
            }
            else -> {
                // Employee can register immediately
                registerEmployee(nextAction)
            }
        }
    }

    override fun afterStoreFormFill(
        name: String,
        location: String,
        phone: String,
        nextAction: () -> Unit
    ) {
        storeName = name
        storeLocation = location
        storePhone = phone
        nextAction()
    }

    override fun onSignWithGoogle(
        token: String,
        nextAction: () -> Unit
    ) {
        signWithGoogleUseCase(
            token = token,
            storePhone = storePhone,
            storeName = storeName,
            storeLocation = storeLocation,
            role = getRole()
        ) { success, message ->
            _isLoading.value = false
            _message.value = message
            if (success) {
                nextAction()
            }
    }
    }


    private fun registerOwner(nextAction: () -> Unit) {
        _isLoading.value = true

        registerOwnerUseCase(
            email = userEmail,
            password = userPassword,
            name = userName,
            storeName = storeName,
            storeLocation = storeLocation,
            storePhone = storePhone
        ) { success, message ->
            _isLoading.value = false
            _message.value = message

            if (success) {
                nextAction()
            }
        }
    }

    private fun registerEmployee(nextAction: () -> Unit) {
        _isLoading.value = true

        registerEmployeeUseCase(
            name = userName,
            email = userEmail,
            password = userPassword
        ) { success, message ->
            _isLoading.value = false
            _message.value = message

            if (success) {
                nextAction()
            }
        }
    }

    fun getRole(): Int = sharedPref.getRole()

    fun isUserDataValid(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isBlank() || email.isBlank() ||
                    password.isBlank() || confirmPassword.isBlank() -> {
                _message.value = "Please fill all fields"
                false
            }
            !email.validEmail() -> {
                _message.value = "Please enter a valid email"
                false
            }
            !password.validPassword() -> {
                _message.value = "Password doesn't meet the requirements"
                false
            }
            !password.validPasswordMatch(confirmPassword) -> {
                _message.value = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    fun isStoreDataValid(
        name: String,
        location: String,
        phone: String
    ): Boolean {
        return when {
            name.isBlank() || location.isBlank() || phone.isBlank() -> {
                _message.value = "Please fill all fields"
                false
            }
            name.length < MIN_STORE_NAME_LENGTH -> {
                _message.value = "Store name must be at least $MIN_STORE_NAME_LENGTH characters"
                false
            }
            !phone.validPhone() -> {
                _message.value = "Please enter a valid phone number"
                false
            }
            else -> true
        }
    }


    companion object {
        private const val MIN_STORE_NAME_LENGTH = 3
    }
}