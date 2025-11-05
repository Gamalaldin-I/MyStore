package com.example.htopstore.ui.adding

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.AddProductUseCase
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import com.example.htopstore.util.BarcodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val addNewProductUseCase: AddProductUseCase
) : ViewModel() {

    // LiveData for UI state
    private val _uiState = MutableLiveData(AddProductUiState())
    val uiState: LiveData<AddProductUiState> = _uiState

    // LiveData for validation messages
    private val _validationMessage = MutableLiveData<String>()
    val validationMessage: LiveData<String> = _validationMessage

    // LiveData for navigation events
    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    // Current product data
    private var currentImageUri: Uri? = null
    private var tempCameraFile: File? = null

    fun setImageUri(uri: Uri?) {
        currentImageUri = uri
        _uiState.value = _uiState.value?.copy(hasImage = uri != null)
    }

    fun getImageUri(): Uri? = currentImageUri

    fun createTempCameraFile(context: Context): File {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "temp_camera_${UUID.randomUUID()}.jpg"
        )
        tempCameraFile = file
        return file
    }

    fun validateAndSaveProduct(
        context: Context,
        category: String,
        brand: String,
        buyingPrice: String,
        sellingPrice: String,
        count: String
    ) {
        viewModelScope.launch {
            if (!validateFields(category, brand, buyingPrice, sellingPrice, count)) {
                return@launch
            }

            _uiState.value = _uiState.value?.copy(isLoading = true)

            try {
                //val savedFile = saveFinalImage(context)
                if (currentImageUri != null) {
                    val product = Product(
                        id = BarcodeGenerator.scannedCode ?: IdGenerator.generateProductId(),
                        addingDate = DateHelper.getCurrentDate(),
                        productImage = currentImageUri.toString(),
                        category = category,
                        name = brand,
                        buyingPrice = buyingPrice.toDouble(),
                        sellingPrice = sellingPrice.toDouble(),
                        count = count.toInt(),
                        soldCount = 0,
                        lastUpdate = DateHelper.getTimeStampMilliSecond(),
                        storeId =""
                    )

                    saveProduct(product)
                } else {
                    _validationMessage.value = "Failed to save image"
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _validationMessage.value = "Error saving product: ${e.message}"
                Log.e("AddProductViewModel", "Error saving product", e)
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    private fun validateFields(
        category: String,
        brand: String,
        buyingPrice: String,
        sellingPrice: String,
        count: String
    ): Boolean {
        if (currentImageUri == null) {
            _validationMessage.value = "Please select or capture an image"
            return false
        }
        if (category.isEmpty()) {
            _validationMessage.value = "Please select a category"
            return false
        }
        if (brand.isEmpty()) {
            _validationMessage.value = "Please enter a brand name"
            return false
        }
        if (buyingPrice.isEmpty()) {
            _validationMessage.value = "Please enter a buying price"
            return false
        }
        if (sellingPrice.isEmpty()) {
            _validationMessage.value = "Please enter a selling price"
            return false
        }
        if (count.isEmpty()) {
            _validationMessage.value = "Please enter a quantity"
            return false
        }

        try {
            val countInt = count.toInt()
            if (countInt <= 0) {
                _validationMessage.value = "Quantity must be greater than 0"
                return false
            }
        } catch (e: NumberFormatException) {
            _validationMessage.value = "Invalid quantity format"
            return false
        }

        try {
            val buyingPriceDouble = buyingPrice.toDouble()
            if (buyingPriceDouble <= 0) {
                _validationMessage.value = "Buying price must be greater than 0"
                return false
            }
        } catch (e: NumberFormatException) {
            _validationMessage.value = "Invalid buying price format"
            return false
        }

        try {
            val sellingPriceDouble = sellingPrice.toDouble()
            if (sellingPriceDouble <= 0) {
                _validationMessage.value = "Selling price must be greater than 0"
                return false
            }

            val buyingPriceDouble = buyingPrice.toDouble()
            if (sellingPriceDouble <= buyingPriceDouble) {
                _validationMessage.value = "Selling price must be greater than buying price"
                return false
            }
        } catch (e: NumberFormatException) {
            _validationMessage.value = "Invalid selling price format"
            return false
        }

        return true
    }

    private suspend fun saveFinalImage(context: Context): File? {
        return withContext(Dispatchers.IO) {
            if (currentImageUri == null) return@withContext null

            try {
                val inputStream = context.contentResolver.openInputStream(currentImageUri!!)
                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "product_${System.currentTimeMillis()}.jpg"
                )

                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)

                inputStream?.close()
                outputStream.close()

                // Clean up temp camera file if it exists
                tempCameraFile?.delete()
                tempCameraFile = null

                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun saveProduct(product: Product) {
        withContext(Dispatchers.IO) {
            try {
                addNewProductUseCase(product)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        productSaved = true,
                        shouldClearForm = true
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _validationMessage.value = "Error saving product: ${e.message}"
                    Log.e("AddProductViewModel", "Error saving product", e)
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            }
        }
    }

    fun resetFormState() {
        currentImageUri = null
        tempCameraFile = null
        BarcodeGenerator.scannedCode = null
        _uiState.value = AddProductUiState()
    }

    fun onBackPressed() {
        _navigationEvent.value = NavigationEvent.NavigateBack
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up temp files when ViewModel is destroyed
        tempCameraFile?.let { file ->
            try {
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class AddProductUiState(
    val isLoading: Boolean = false,
    val hasImage: Boolean = false,
    val productSaved: Boolean = false,
    val shouldClearForm: Boolean = false
)

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}