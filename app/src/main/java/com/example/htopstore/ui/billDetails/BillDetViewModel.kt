package com.example.htopstore.ui.billDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.domain.model.User
import com.example.domain.useCase.bill.DeleteBillUseCase
import com.example.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.domain.useCase.billDetails.ReturnProductUseCase
import com.example.domain.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillDetViewModel @Inject constructor(
    private val pref: SharedPref,
    private val supabaseClient: SupabaseClient,
    private val getBillDetails: GetBillDetailsUseCse,
    private val insertReturnProduct: ReturnProductUseCase,
    private val deleteBillUseCase: DeleteBillUseCase
) : ViewModel() {

    private val _sellOp = MutableLiveData<BillWithDetails>()
    val sellOp: LiveData<BillWithDetails> = _sellOp

    private val _employee = MutableLiveData<User>()
    val employee: LiveData<User> = _employee

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getBill(id: String, confirmDeleteIfEmpty: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                // Get bill details
                val billWithDetails = getBillDetails(id)

                // Get employee information
                val employeeData = try {
                    getBillEmployee(billWithDetails.bill.userId)
                } catch (e: Exception) {
                    Log.e("BillDetViewModel", "Error fetching employee: ${e.message}")
                    // If employee fetch fails, create a default user
                    User(
                        id = billWithDetails.bill.userId,
                        name = "Unknown Employee",
                        email = "",
                        role = 3,
                        photoUrl ="",
                        status = Constants.STATUS_FIRED,
                        storeId = "",
                        provider = ""
                    )
                }

                withContext(Dispatchers.Main) {
                    _sellOp.value = billWithDetails
                    _employee.value = employeeData
                    _isLoading.value = false

                    // Check if bill has no products
                    if (billWithDetails.soldProducts.isEmpty()) {
                        confirmDeleteIfEmpty()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _error.value = "Failed to load bill details: ${e.message}"
                }
            }
        }
    }

    private suspend fun getBillEmployee(userId: String): User {
        return try {
            if(userId==pref.getUser().id) return pref.getUser()
            supabaseClient.from("users").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
        } catch (e: Exception) {
            Log.e("BillDetViewModel", "Error fetching employee: ${e.message}")
            // Return default user if query fails
            throw e
        }
    }

    fun onClick(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        confirmDeleteIfEmpty: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                when (val result = insertReturnProduct(soldProduct, returnRequest)) {
                    is ReturnProductUseCase.ReturnResult.Error -> {
                        withContext(Dispatchers.Main) {
                            _isLoading.value = false
                            _message.value = "Return failed: ${result.message}"
                        }
                    }

                    is ReturnProductUseCase.ReturnResult.Success -> {
                        withContext(Dispatchers.Main) {
                            _message.value = result.message
                        }

                        // Refresh bill data
                        soldProduct.billId?.let { billId ->
                            getBill(billId) {
                                confirmDeleteIfEmpty()
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                _isLoading.value = false
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _error.value = "Failed to process return: ${e.message}"
                }
            }
        }
    }

    fun deleteBill(id: String, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                deleteBillUseCase(id)

                // Small delay to show loading state
                delay(300)

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onFinish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _error.value = "Failed to delete bill: ${e.message}"
                }
            }
        }
    }

    // Optional: Add method to refresh bill data
    fun refreshBill(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                val billWithDetails = getBillDetails(id)
                val employeeData = getBillEmployee(billWithDetails.bill.userId)

                withContext(Dispatchers.Main) {
                    _sellOp.value = billWithDetails
                    _employee.value = employeeData
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _error.value = "Failed to refresh bill: ${e.message}"
                }
            }
        }
    }

    // Clear error message after it's been shown
    fun clearError() {
        _error.value = ""
    }

    // Clear message after it's been shown
    fun clearMessage() {
        _message.value = ""
    }
}