package com.example.htopstore.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Expense
import com.example.domain.useCase.expenses.GetAllExpensesUseCase
import com.example.domain.useCase.expenses.InsertNewExpenseUseCase
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val insertNewExpenseUseCase: InsertNewExpenseUseCase
) : ViewModel() {

    // LiveData for expenses list
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData for success messages (when expense is added)
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // LiveData for total expenses amount
    private val _totalAmount = MutableLiveData<Double>()
    val totalAmount: LiveData<Double> = _totalAmount

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expensesList = getAllExpensesUseCase()
                _expenses.postValue(expensesList)
                _totalAmount.postValue(calculateTotalAmount(expensesList))
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load expenses: ${e.message}")
                _expenses.postValue(emptyList())
                _totalAmount.postValue(0.0)
                _isLoading.postValue(false)
            }
        }
    }

    fun addExpense(category: String, description: String, amount: Double, paymentMethod: String) {
        if (validateExpenseInput(category, description, amount)) {
            val expense = Expense(
                expenseId = IdGenerator.generateTimestampedId(),
                date = DateHelper.getCurrentDate(),
                time = DateHelper.getCurrentTime(),
                description = description,
                category = category,
                amount = amount,
                paymentMethod = paymentMethod
            )

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    insertNewExpenseUseCase(expense)

                    // Update the current list
                    val currentList = _expenses.value?.toMutableList() ?: mutableListOf()
                    currentList.add(0, expense) // Add at the beginning for newest first

                    _expenses.postValue(currentList)
                    _totalAmount.postValue(calculateTotalAmount(currentList))
                    _successMessage.postValue("Expense added successfully")

                } catch (e: Exception) {
                    _errorMessage.postValue("Failed to add expense: ${e.message}")
                }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                //expensesRepo.(expense)

                // Update the current list
                val currentList = _expenses.value?.toMutableList() ?: mutableListOf()
                currentList.remove(expense)

                _expenses.postValue(currentList)
                _totalAmount.postValue(calculateTotalAmount(currentList))
                _successMessage.postValue("Expense deleted successfully")

            } catch (e: Exception) {
                _errorMessage.postValue("Failed to delete expense: ${e.message}")
            }
        }
    }

    fun refreshExpenses() {
        loadExpenses()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun validateExpenseInput(category: String, description: String, amount: Double): Boolean {
        return when {
            category.isBlank() -> {
                _errorMessage.value = "Please select a category"
                false
            }
            description.isBlank() -> {
                _errorMessage.value = "Please enter a description"
                false
            }
            amount <= 0 -> {
                _errorMessage.value = "Please enter a valid amount"
                false
            }
            else -> true
        }
    }

    private fun calculateTotalAmount(expenses: List<Expense>): Double {
        return expenses.sumOf { it.amount ?: 0.0 }
    }
}