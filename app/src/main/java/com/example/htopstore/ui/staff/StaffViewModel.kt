package com.example.htopstore.ui.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.User
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.useCase.invitations.AddStoreInviteUseCase
import com.example.domain.useCase.invitations.DeleteStoreInviteUseCase
import com.example.domain.useCase.invitations.GetStoreInvitesUseCase
import com.example.domain.useCase.invitations.SendInvitationMailUseCase
import com.example.domain.useCase.staff.GetStoreEmployeesUseCase
import com.example.domain.useCase.staff.RejectOrRehireUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for managing store staff and invitations.
 * Handles data loading, CRUD operations, and business logic.
 */
@HiltViewModel
class StaffViewModel @Inject constructor(
    private val getStoreEmployeesUseCase: GetStoreEmployeesUseCase,
    private val getStoreInvitesUseCase: GetStoreInvitesUseCase,
    private val deleteStoreInviteUseCase: DeleteStoreInviteUseCase,
    private val addStoreInviteUseCase: AddStoreInviteUseCase,
    private val rejectOrRehireUseCase: RejectOrRehireUseCase,
    private val sendInvitationMailUseCase: SendInvitationMailUseCase
) : ViewModel() {

    // LiveData for UI state
    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    private val _invites = MutableLiveData<List<Invitation>>()
    val invites: LiveData<List<Invitation>> = _invites

    private val _employees = MutableLiveData<List<User>>()
    val employees: LiveData<List<User>> = _employees

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ========================================================================
    // Employee Operations
    // ========================================================================

    /**
     * Fetch all store employees
     */
    fun getEmployees() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val (employeeList, message) = withContext(Dispatchers.IO) {
                    getStoreEmployeesUseCase()
                }

                _employees.value = employeeList

                if (message.isNotEmpty()) {
                    _msg.value = message
                }
            } catch (e: Exception) {
                _msg.value = "Failed to load employees: ${e.message}"
                _employees.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Hire or fire an employee
     * @param employeeId ID of the employee
     * @param shouldFire true to fire, false to hire
     */
    fun hireOrFire(employeeId: String, shouldFire: Boolean) {
        viewModelScope.launch {
            try {
                val (success, message) = withContext(Dispatchers.IO) {
                    rejectOrRehireUseCase(employeeId, shouldFire)
                }

                if (success) {
                    // Refresh employee list after successful operation
                    getEmployees()
                    _msg.value = message.ifEmpty {
                        if (shouldFire) "Employee fired" else "Employee hired"
                    }
                } else {
                    _msg.value = message
                }
            } catch (e: Exception) {
                _msg.value = "Operation failed: ${e.message}"
            }
        }
    }

    // ========================================================================
    // Invitation Operations
    // ========================================================================

    /**
     * Fetch all store invitations
     */
    fun getAllStoreInvites() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val (inviteList) = withContext(Dispatchers.IO) {
                    getStoreInvitesUseCase()
                }

                _invites.value = inviteList
            } catch (e: Exception) {
                _msg.value = "Failed to load invitations: ${e.message}"
                _invites.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new invitation
     * @param email Email address for the invitation
     * @param code Unique invite code
     * @param onSuccess Callback to execute on success (runs on Main thread)
     */
    fun addInvite(
        email: String,
        code: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val (success, message) = withContext(Dispatchers.IO) {
                    addStoreInviteUseCase(email, code)
                }

                _msg.value = message

                if (success) {
                    // Refresh invitations list
                    getAllStoreInvites()

                    // Execute success callback on Main thread
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                _msg.value = "Failed to create invite: ${e.message}"
            }
        }
    }

    /**
     * Delete an invitation
     * @param invite Invitation to delete
     * @param onSuccess Callback to execute on success (runs on Main thread)
     */
    fun deleteInvite(
        invite: Invitation,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val (success, message) = withContext(Dispatchers.IO) {
                    deleteStoreInviteUseCase(invite)
                }

                _msg.value = message

                if (success) {
                    // Refresh invitations list
                    getAllStoreInvites()

                    // Execute success callback on Main thread
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                _msg.value = "Failed to delete invite: ${e.message}"
            }
        }
    }

    /**
     * Send invitation email
     * @param recipientEmail Email address of the recipient
     * @param code Invite code to include in email
     */
    fun sendEmail(code: String,onRes: (subject:String,body:String) -> Unit) {
        val(sub,body) = sendInvitationMailUseCase(code)
        onRes(sub,body)
    }


    /**
     * Clear message LiveData
     * Useful for preventing message re-display on configuration changes
     */
    fun clearMessage() {
        _msg.value = ""
    }
}