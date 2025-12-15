package com.example.htopstore.ui.pendingProducts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.AddPendingProductsUseCase
import com.example.domain.useCase.product.DeletePendingProductUseCase
import com.example.domain.useCase.product.GetPendingProductsYUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    data class Loading(val progress: Int, val currentItem: Int, val totalItems: Int) : UploadState()
    data class Success(val message: String, val itemCount: Int) : UploadState()
    data class Error(val message: String, val details: String? = null) : UploadState()
}

@HiltViewModel
class PendingProductsViewModel @Inject constructor(
    private val addPendingProductsUseCase: AddPendingProductsUseCase,
    getPendingProductsYUseCase: GetPendingProductsYUseCase,
    private val deletePendingProductUseCase: DeletePendingProductUseCase
) : ViewModel() {

    val products: LiveData<List<Product>> = getPendingProductsYUseCase().asLiveData()

    private val _uploadState = MutableLiveData<UploadState>(UploadState.Idle)
    val uploadState: LiveData<UploadState> = _uploadState

    fun deleteProductById(id: String, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deleted = deletePendingProductUseCase(id)
                if (deleted) {
                    withContext(Dispatchers.Main) {
                        onFinish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uploadState.value = UploadState.Error(
                        "Failed to delete product",
                        e.message
                    )
                }
            }
        }
    }

    fun uploadPendingProducts() {
        val productList = products.value

        if (productList.isNullOrEmpty()) {
            _uploadState.value = UploadState.Error(
                "No products to upload",
                "Please add products before uploading"
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val totalItems = productList.size

                withContext(Dispatchers.Main) {
                    _uploadState.value = UploadState.Loading(0, 0, totalItems)
                }

                addPendingProductsUseCase(
                    productList,
                    onProgress = { progress ->
                        viewModelScope.launch(Dispatchers.Main) {
                            val currentItem = ((progress / 100f) * totalItems).toInt()
                            _uploadState.value = UploadState.Loading(progress, currentItem, totalItems)
                        }
                    }
                ) {
                    viewModelScope.launch(Dispatchers.Main) {
                        // Small delay for final animation
                        delay(300)
                        _uploadState.value = UploadState.Success(
                            "Products uploaded successfully",
                            totalItems
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uploadState.value = UploadState.Error(
                        "Upload failed",
                        e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}