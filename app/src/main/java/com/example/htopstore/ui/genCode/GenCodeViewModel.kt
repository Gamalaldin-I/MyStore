package com.example.htopstore.ui.genCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenCodeViewModel
    @Inject constructor(
        private val getAvailableProductsUseCase: GetAvailableProductsUseCase
    ): ViewModel(){

        private val _products: MutableLiveData<List<Product>> = MutableLiveData()
        val products : LiveData<List<Product>> = _products

        fun getAvailableProducts() = viewModelScope.launch(Dispatchers.IO){
            _products.postValue(getAvailableProductsUseCase())
        }


}