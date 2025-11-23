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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val insertNewExpenseUseCase: InsertNewExpenseUseCase
) : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    private fun insertNewExpense(expense: Expense, onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            insertNewExpenseUseCase(expense)
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
    fun validate(
        amount: String,
        category: String,
        method: String,
        description: String,
        onFinished: () -> Unit
    ){
        if (amount.isEmpty() || category.isEmpty() || method.isEmpty() || description.isEmpty()){
            _message.value = "Please fill all fields"
            return
        }
        val expense = Expense(
            id = IdGenerator.generateTimestampedId(),
            time = DateHelper.getCurrentTime(),
            date = DateHelper.getCurrentDate(),
            amount = amount.toDouble(),
            category = category,
            paymentMethod = method,
            description = description,
            storeId = "",
            lastUpdate = DateHelper.getCurrentTimestampTz(),
            userId = "",
            deleted = false)
        insertNewExpense(expense, onFinished)
    }

}