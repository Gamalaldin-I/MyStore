package com.example.htopstore.ui.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.domain.useCase.stock.GetStockProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class StockViewModel @Inject constructor(
    private val getStockProducts : GetStockProductsUseCase
)
    : ViewModel(){
        private var _products = MutableLiveData<List<Product>>()
        val products : LiveData<List<Product>> = _products


    fun getProducts(){
        viewModelScope.launch(Dispatchers.IO){
            _products.postValue(getStockProducts())
    }
    }

}