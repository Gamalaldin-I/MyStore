package com.example.htopstore.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Expense
import com.example.domain.useCase.expenses.InsertNewExpenseUseCase
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val insertNewExpenseUseCase: InsertNewExpenseUseCase
) : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private fun insertNewExpense(expense: Expense, onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                // Call the use case
                val (success, msg) = insertNewExpenseUseCase(expense)

                // Small delay to show loading animation (minimum 500ms)
                // Remove this if your actual operation takes longer
                delay(500)

                withContext(Dispatchers.Main) {
                    _isLoading.value = false

                    if (success) {
                        _isSuccess.value = true
                        _message.value = msg
                        onFinished()
                    } else {
                        _message.value = msg ?: "Failed to add expense"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _message.value = "Error: ${e.message}"
                }
            }
        }
    }

    fun validate(
        amount: String,
        category: String,
        method: String,
        description: String,
        onFinished: () -> Unit
    ) {
        // Validation
        when {
            amount.isEmpty() -> {
                _message.value = "Please enter an amount"
                return
            }
            category.isEmpty() -> {
                _message.value = "Please select a category"
                return
            }
            method.isEmpty() -> {
                _message.value = "Please select a payment method"
                return
            }
            amount.toDoubleOrNull() == null -> {
                _message.value = "Please enter a valid amount"
                return
            }
            amount.toDouble() <= 0 -> {
                _message.value = "Amount must be greater than zero"
                return
            }
        }

        // Create expense object
        val expense = Expense(
            id = IdGenerator.generateTimestampedId(),
            time = DateHelper.getCurrentTime(),
            date = DateHelper.getCurrentDate(),
            amount = amount.toDouble(),
            category = category,
            paymentMethod = method,
            description = description.ifEmpty { "" },
            storeId = "",
            lastUpdate = DateHelper.getCurrentTimestampTz(),
            userId = "",
            deleted = false
        )

        insertNewExpense(expense, onFinished)
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }
}