package com.example.htopstore.ui.changePassword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.ChangePasswordUseCase
import com.example.domain.useCase.auth.ResetPasswordUseCase
import com.example.htopstore.ui.changePassword.ChangePasswordActivity.PasswordStrength
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ChangePasswordViewModel
    @Inject constructor(
        private val changePasswordUseCase: ChangePasswordUseCase,
        private val resetPasswordUseCase: ResetPasswordUseCase,
        private val pref: SharedPref
    ):ViewModel(){
        private val _msg = MutableLiveData<String>()
        val msg: MutableLiveData<String> = _msg

    fun changePassword(currentPassword: String, newPassword: String,onSuccess:()->Unit) {
        changePasswordUseCase(currentPassword, newPassword) { success, msg ->
            if (success) {
                onSuccess()
            }
            _msg.value = msg
        }
    }
    fun forgotPassword(onSuccess: () -> Unit) {
        resetPasswordUseCase(pref.getUser().email){ success, msg ->
            if (success) {
                onSuccess()
            }
        }
    }

    private val _currentError = MutableLiveData<String>()
    val currentError: MutableLiveData<String> = _currentError
    private val _newError = MutableLiveData<String>()
    val newError: MutableLiveData<String> = _newError
    private val _confirmError = MutableLiveData<String>()
    val confirmError: MutableLiveData<String> = _confirmError

    fun validateInputs(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validate current password
        if (currentPassword.isEmpty()) {
            _currentError.value = "Please enter your current password"
            isValid = false
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            _newError.value = "Please enter a new password"
            isValid = false
        } else if (newPassword.length < 8) {
            _newError.value = "Password must be at least 8 characters"
            isValid = false
        } else if (!newPassword.any { it.isUpperCase() }) {
            _newError.value = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!newPassword.any { it.isDigit() }) {
            _newError.value = "Password must contain at least one number"
            isValid = false
        } else if (newPassword == currentPassword) {
            _newError.value = "New password must be different from current password"
            isValid = false
        }

        //Validate confirm password
        if (confirmPassword.isEmpty()) {
           _confirmError.value = "Please confirm your new password"
            isValid = false
        } else if (confirmPassword != newPassword) {
            _confirmError.value = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
    fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0

        // Length check
        when {
            password.length >= 12 -> score += 30
            password.length >= 8 -> score += 20
            password.length >= 6 -> score += 10
        }

        // Uppercase check
        if (password.any { it.isUpperCase() }) score += 15

        // Lowercase check
        if (password.any { it.isLowerCase() }) score += 15

        // Digit check
        if (password.any { it.isDigit() }) score += 15

        // Special character check
        if (password.any { !it.isLetterOrDigit() }) score += 15

        // Variety check
        val uniqueChars = password.toSet().size
        if (uniqueChars >= password.length * 0.7) score += 10

        val label = when (score) {
            in 0..33 -> "Weak"
            in 34..66 -> "Medium"
            else -> "Strong"
        }

        return PasswordStrength(score, label)
    }

}