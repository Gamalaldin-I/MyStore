package com.example.htopstore.ui.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.RegisterUseCase
import com.example.domain.useCase.auth.SignWithGoogleUseCase
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.R
import com.example.htopstore.util.DataValidator.validEmail
import com.example.htopstore.util.DataValidator.validPassword
import com.example.htopstore.util.DataValidator.validPasswordMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val registerUseCase: RegisterUseCase,
    private val signWithGoogleUseCase: SignWithGoogleUseCase,
    private val app: Application
) : AndroidViewModel(app), OnNextStep {
    companion object{
        const val MAIN_ACTIVITY = "main_activity"
        const val CREATE_STORE_ACTIVITY = "create_store_activity"
        const val INBOX_ACTIVITY = "inbox_activity"
    }

    // User credentials
    private var userName: String = ""
    private var userEmail: String = ""
    private var userPassword: String = ""


    // LiveData for UI feedback
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading


    private val _goTo = MutableLiveData<String>()
    val goTo: LiveData<String> = _goTo

    fun goto(){
        val storeId = sharedPref.getStore().id
        val role = sharedPref.getUser().role
        val isLogin = sharedPref.isLogin()
        if (storeId.isNotEmpty()&&isLogin){
            _goTo.value = MAIN_ACTIVITY
        }
        else if(role == OWNER_ROLE&& isLogin) {
            _goTo.value = CREATE_STORE_ACTIVITY
        }
        else if(role != OWNER_ROLE && isLogin){
            _goTo.value = INBOX_ACTIVITY }
    }




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
        if (isUserDataValid(name, email, password, password)) {
            register(nextAction)
        }
    }


    override fun onSignWithGoogle(
        token: String,
        profileUrl:String,
        name: String,
        nextAction: () -> Unit
    ) {
        viewModelScope.launch {
        sharedPref.setProfileImage(profileUrl)
        sharedPref.setUserName(name)
        val(success,message) = signWithGoogleUseCase(
            token = token,
            role = getRole(),
            fromLoginScreen = false
        )
            _isLoading.value = false
            _message.postValue(message)
            if (success) withContext(Dispatchers.Main){ nextAction()}
        }
    }


    private fun register(nextAction: () -> Unit) {
        _isLoading.value = true

        registerUseCase(
            email = userEmail,
            password = userPassword,
            name = userName,
            role = getRole()
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
                _message.value = app.getString(R.string.please_fill_all_fields)
                false
            }
            !email.validEmail() -> {
                _message.value = app.getString(R.string.error_invalid_email)
                false
            }
            !password.validPassword() -> {
                _message.value = app.getString(R.string.password_requirements_not_met)
                false
            }
            !password.validPasswordMatch(confirmPassword) -> {
                _message.value = app.getString(R.string.passwords_do_not_match)
                false
            }
            else -> true
        }
    }


}