package com.example.htopstore.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.product.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase
):ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message



    fun onScanned(id:String) =

        viewModelScope.launch(Dispatchers.IO){
            val product = getProductByIdUseCase(id)
            if (product != null) {
                _message.postValue("Product found: ${product.name}")
                // Handle the product as needed
            } else {
                _message.postValue("Product not found")
            }
        }
}