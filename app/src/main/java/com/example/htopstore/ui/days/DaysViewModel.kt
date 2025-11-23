package com.example.htopstore.ui.days

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.analisys.GetDaysOfWorkUseCase
import com.example.domain.useCase.analisys.GetSpecificDayUseCase
import com.example.domain.useCase.bill.FetchAllNewBillsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaysViewModel @Inject constructor(
    private val getDaysOfWorkUseCase: GetDaysOfWorkUseCase,
    private val getSpecificDayUseCase: GetSpecificDayUseCase,
    private val fetchBillsAndSalesFromRemoteUseCase: FetchAllNewBillsUseCase
) : ViewModel() {

    private val _days = MutableLiveData<List<String>>()
    val days: LiveData<List<String>> = _days

    private val _specificDay: MutableLiveData<String> = MutableLiveData()
    val specificDay: LiveData<String> = _specificDay

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getDays() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                _days.postValue(getDaysOfWorkUseCase())
            } catch (e: Exception) {
                _message.postValue("Error loading days: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getSpecificDay(day: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                _specificDay.postValue(getSpecificDayUseCase(day))
            } catch (e: Exception) {
                _message.postValue("Error loading day: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchBillsAndSalesFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val (success, msg) = fetchBillsAndSalesFromRemoteUseCase()
                if (!success) {
                    _message.postValue(msg)
                }
                getDays()
            } catch (e: Exception) {
                _message.postValue("Error fetching data: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}