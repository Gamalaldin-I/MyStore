package com.example.htopstore.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.util.CartHelper
import com.example.htopstore.util.BarcodeGenerator
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
                CartHelper.addToTheCartList(product)
                // Handle the product as needed
            } else {
                _message.postValue("Product not found")
            }
        }

    fun onAddProduct(scannedId:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = getProductByIdUseCase(scannedId)
            if (product != null) {
                _message.postValue("Product found: ${product.name}")
                BarcodeGenerator.scannedCode = null
            } else {
                BarcodeGenerator.scannedCode = scannedId
            }
        }
}
}