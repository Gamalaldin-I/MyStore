package com.example.htopstore.ui.emlpoyee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.staff.ChangeEmpRoleUseCase
import com.example.domain.useCase.staff.RejectOrRehireUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val fireOrRehireEmployeeUseCase: RejectOrRehireUseCase,
    private val changeEmpRoleUseCase: ChangeEmpRoleUseCase
) : ViewModel() {

    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading



    fun changeEmployeeRole(employeeId: String, newRole: Int,onRoleChanged: (String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = changeEmpRoleUseCase(newRole=newRole, empId = employeeId)
                if (result.first) {
                    onRoleChanged(result.second)
                }
                _msg.postValue(result.second)
                _loading.postValue(false)

            } catch (e: Exception) {
                    e.message ?: "An unexpected error occurred"
                    _loading.postValue(false)
            }
            }
        }

    fun hireOrFire(employeeId: String, shouldFire: Boolean) {
        viewModelScope.launch {
            try {
                val (success, message) = withContext(Dispatchers.IO) {
                    fireOrRehireEmployeeUseCase(employeeId, shouldFire)
                }

                if (success) {
                    // Refresh employee list after successful operation
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
}