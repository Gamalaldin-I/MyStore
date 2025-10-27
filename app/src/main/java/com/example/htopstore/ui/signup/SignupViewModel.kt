package com.example.htopstore.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.useCase.auth.RegisterEmployeeUseCase
import com.example.domain.useCase.auth.RegisterOwnerUseCase
import com.example.domain.util.Constants
import com.example.htopstore.util.DataValidator.validEmail
import com.example.htopstore.util.DataValidator.validPassword
import com.example.htopstore.util.DataValidator.validPasswordMatch
import com.example.htopstore.util.DataValidator.validPhone
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing the signup flow for both store owners and employees.
 * Handles form validation, role selection, and user registration.
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val registerOwnerUseCase: RegisterOwnerUseCase,
    private val registerEmployeeUseCase: RegisterEmployeeUseCase
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

    /**
     * Handles role selection and proceeds to the appropriate form.
     * @param role The selected role (OWNER_ROLE or EMPLOYEE_ROLE)
     * @param nextAction Callback to navigate to the next screen
     */
    override fun afterRoleSelection(role: Int, nextAction: () -> Unit) {
        sharedPref.setRole(role)
        nextAction()
    }

    /**
     * Handles user form submission and triggers registration based on role.
     * For owners: Stores data temporarily and proceeds to store form
     * For employees: Immediately registers the user
     *
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     * @param nextAction Callback to navigate to the next screen on success
     */
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

    /**
     * Handles store form submission and stores the data.
     * @param name Store name
     * @param location Store location/address
     * @param phone Store phone number
     * @param nextAction Callback to navigate to the next screen
     */
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

    /**
     * Registers a new store owner with complete store information.
     * @param nextAction Callback invoked on successful registration
     */
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

    /**
     * Registers a new employee.
     * @param nextAction Callback invoked on successful registration
     */
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

    /**
     * Gets the currently selected user role.
     * @return Role constant (OWNER_ROLE or EMPLOYEE_ROLE)
     */
    fun getRole(): Int = sharedPref.getRole()

    /**
     * Validates user registration form data.
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return true if all fields are valid, false otherwise
     */
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

    /**
     * Validates store registration form data.
     * @param name Store name
     * @param location Store location/address
     * @param phone Store phone number
     * @return true if all fields are valid, false otherwise
     */
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

    /**
     * Validates verification code format.
     * @param code The verification code to validate
     * @return true if code is valid, false otherwise
     */
    fun isVerificationCodeValid(code: String): Boolean {
        return when {
            code.isBlank() -> {
                _message.value = "Please enter the verification code"
                false
            }
            code.length != VERIFICATION_CODE_LENGTH -> {
                _message.value = "Verification code must be $VERIFICATION_CODE_LENGTH digits"
                false
            }
            else -> {
                _message.value = "Sending verification code..."
                true
            }
        }
    }

    /**
     * Clears all stored user and store data.
     * Useful for resetting the signup flow or handling errors.
     */
    fun clearFormData() {
        userName = ""
        userEmail = ""
        userPassword = ""
        storeName = ""
        storeLocation = ""
        storePhone = ""
    }

    companion object {
        private const val MIN_STORE_NAME_LENGTH = 3
        private const val VERIFICATION_CODE_LENGTH = 6
    }
}