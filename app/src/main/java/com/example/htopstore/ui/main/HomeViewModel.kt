package com.example.htopstore.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.domain.model.DayBrief
import com.example.htopstore.domain.useCase.home.GetLowStockUseCase
import com.example.htopstore.domain.useCase.home.GetTodayBriefUseCase
import com.example.htopstore.domain.useCase.home.GetTop5InSalesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLowStockUseCase: GetLowStockUseCase,
    private val getTop5InSalesUseCase: GetTop5InSalesUseCase,
    private val getTodayBriefUseCase: GetTodayBriefUseCase
): ViewModel() {

    // Private mutable live data
    private val _top5: MutableLiveData<List<Product>> = MutableLiveData()
    val top5: MutableLiveData<List<Product>> = _top5

    private val _lowStock: MutableLiveData<List<Product>> = MutableLiveData()
    val lowStock: MutableLiveData<List<Product>> = _lowStock

    private val _todayBrief: MutableLiveData<DayBrief> = MutableLiveData()
    val todayBrief: MutableLiveData<DayBrief> = _todayBrief

    fun getTop5InSales() {
        viewModelScope.launch(Dispatchers.IO) {
            _top5.postValue(getTop5InSalesUseCase())
        }
    }

    fun getLowStock() {
        viewModelScope.launch(Dispatchers.IO) {
            _lowStock.postValue(getLowStockUseCase())
        }
    }

    fun getTodayBrief() {
        viewModelScope.launch(Dispatchers.IO) {
            _todayBrief.postValue(getTodayBriefUseCase())
        }
    }
}