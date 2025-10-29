package com.example.htopstore.ui.updateStore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Store
import com.example.domain.useCase.auth.UpdateStoreDataUseCase
import com.example.htopstore.util.AuthChecker.isValidPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UpdateStoreViewModel @Inject constructor(
    private val updateStoreDataUseCase: UpdateStoreDataUseCase,
    private val pref: SharedPref
) : ViewModel() {


    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _msg = MutableLiveData<String>()
    val msg: LiveData<String> = _msg

    fun loadStoreData():Store{
        return pref.getStore()
    }

    fun updateStore(name:String,
                    location:String,
                    phone:String,
                    actionOnSuccess: () -> Unit) {
        // Validate first
        if (!validateStoreData(
                name = name,
                location = location,
                phone = phone
        )) {
            _msg.value ="Please fill all required fields correctly"
            return
        }
        _isLoading.value = true
                updateStoreDataUseCase(
                    name = name.trim(),
                    phone = phone.trim(),
                    location = location.trim(),
                ) { success, msg ->
                    if (success) {
                        val currentStore = pref.getStore()
                        pref.saveStore(
                            id = currentStore.id,
                            name = name.trim(),
                            phone = phone.trim(),
                            location = location.trim(),
                            ownerId = currentStore.ownerId
                        )
                        actionOnSuccess()
                    }
                    _msg.value = msg
                    _isLoading.value = false
                }
    }

    private fun validateStoreData(name:String, location:String, phone:String): Boolean {
        return name.trim().length >= 3 &&
                isValidPhoneNumber(phone.trim()) &&
                location.trim().length > 5
    }
}