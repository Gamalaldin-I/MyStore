package com.example.htopstore.ui.days

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.analisys.GetDaysOfWorkUseCase
import com.example.domain.useCase.analisys.GetSpecificDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaysViewModel
    @Inject constructor(
    private val getDaysOfWorkUseCase: GetDaysOfWorkUseCase,
    private val getSpecificDayUseCase: GetSpecificDayUseCase

    ):ViewModel() {

    private val _days = MutableLiveData<List<String>>()
    val days: LiveData<List<String>> = _days

    private val _specificDay : MutableLiveData<String> = MutableLiveData()
    val specificDay: LiveData<String> =  _specificDay


    fun getDays(){
        viewModelScope.launch(Dispatchers.IO){
             _days.postValue(getDaysOfWorkUseCase())
            }
        }
    fun getSpecificDay(day:String){
        viewModelScope.launch {
            _specificDay.postValue(getSpecificDayUseCase(day)) }
    }
}