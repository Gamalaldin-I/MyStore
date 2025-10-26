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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase
) : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _found = MutableLiveData<Boolean>(true)
    val found: LiveData<Boolean> = _found


    fun onScanned(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val product = getProductByIdUseCase(id)

                withContext(Dispatchers.Main) {
                    if (product != null) {
                        _message.value = "✓ Product found: ${product.name}"
                        _found.value = true
                        CartHelper.addToTheCartList(product)
                    } else {
                        _message.value = "✗ Product not found"
                        _found.value = false
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Error: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun onAddProduct(scannedId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val product = getProductByIdUseCase(scannedId)

                withContext(Dispatchers.Main) {
                    if (product != null) {
                        _message.value = "✓ Product already exists: ${product.name}"
                        _found.value = false
                        BarcodeGenerator.scannedCode = null
                    } else {
                        _message.value = "✓ New product ID scanned"
                        BarcodeGenerator.scannedCode = scannedId
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Error: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
}