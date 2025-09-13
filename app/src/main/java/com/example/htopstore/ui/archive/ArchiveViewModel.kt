package com.example.htopstore.ui.archive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.domain.useCase.archive.DeleteProductUseCase
import com.example.htopstore.domain.useCase.archive.GetArchiveProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getArchiveProductsUseCase: GetArchiveProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
): ViewModel() {
    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private var _deleted = MutableLiveData<Boolean>()
    val deleted: LiveData<Boolean> = _deleted

    fun getArchiveProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _products.postValue(getArchiveProductsUseCase())
        }
    }
    fun deleteProduct(productId: String,imageUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteProductUseCase(productId,imageUri)
            _deleted.postValue(true)
        }
    }

}