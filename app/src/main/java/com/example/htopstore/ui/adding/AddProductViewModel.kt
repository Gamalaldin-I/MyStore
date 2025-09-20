package com.example.htopstore.ui.adding

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.AddProductUseCase
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel
    @Inject constructor(
    private val addNewProductUseCase: AddProductUseCase)
     : ViewModel() {

    // LiveData for UI state
    private val _uiState = MutableLiveData<AddProductUiState>()
    val uiState: LiveData<AddProductUiState> = _uiState

    // LiveData for validation messages
    private val _validationMessage = MutableLiveData<String>()
    val validationMessage: LiveData<String> = _validationMessage

    // LiveData for navigation events
    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    // Current product data
    private var productId: String = IdGenerator.generateProductId()
    private var tempImageFile: File? = null
    private var tempImageUri: Uri? = null


    fun setTempImageData(file: File?, uri: Uri?) {
        tempImageFile = file
        tempImageUri = uri
        _uiState.value = _uiState.value?.copy(hasImage = uri != null) ?:
                AddProductUiState(hasImage = uri != null)
    }

    fun getTempImageUri(): Uri? = tempImageUri

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

            _uiState.value = _uiState.value?.copy(isLoading = true) ?:
                    AddProductUiState(isLoading = true)

            try {
                val savedFile = saveFinalImage(context)
                if (savedFile != null) {
                    val product = Product(
                        id = productId,
                        addingDate = DateHelper.getCurrentDate(),
                        productImage = savedFile.absolutePath,
                        category = category,
                        name = brand,
                        buyingPrice = buyingPrice.toDouble(),
                        sellingPrice = sellingPrice.toDouble(),
                        count = count.toInt(),
                        soldCount = 0
                    )

                    saveProduct(product)
                } else {
                    _validationMessage.value = "Failed to save image"
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _validationMessage.value = "Error saving product: ${e.message}"
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
        if (tempImageUri == null) {
            _validationMessage.value = "Please capture an image"
            return false
        }
        if (category.isEmpty()) {
            _validationMessage.value = "Please select a type"
            return false
        }
        if (brand.isEmpty()) {
            _validationMessage.value = "Please enter a brand"
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
            _validationMessage.value = "Please enter a count"
            return false
        }

        try {
            val countInt = count.toInt()
            if (countInt <= 0) {
                _validationMessage.value = "Invalid count"
                return false
            }
        } catch (e: NumberFormatException) {
            _validationMessage.value = "Invalid count format"
            return false
        }

        try {
            val buyingPriceDouble = buyingPrice.toDouble()
            if (buyingPriceDouble <= 0) {
                _validationMessage.value = "Invalid buying price"
                return false
            }
        } catch (_: NumberFormatException) {
            _validationMessage.value = "Invalid buying price format"
            return false
        }

        try {
            val sellingPriceDouble = sellingPrice.toDouble()
            if (sellingPriceDouble <= 0) {
                _validationMessage.value = "Invalid selling price"
                return false
            }

            val buyingPriceDouble = buyingPrice.toDouble()
            if (sellingPriceDouble <= buyingPriceDouble) {
                _validationMessage.value = "Selling price must be > buying price"
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
            if (tempImageUri == null) return@withContext null

            try {
                val inputStream = context.contentResolver.openInputStream(tempImageUri!!)
                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "product_${System.currentTimeMillis()}.jpg"
                )

                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)

                inputStream?.close()
                outputStream.close()

                // Clean up temp file
                tempImageFile?.delete()
                tempImageFile = null

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
                    resetForm()
                    _uiState.value = _uiState.value?.copy(isLoading = false, productSaved = true) ?:
                            AddProductUiState(isLoading = false, productSaved = true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _validationMessage.value = "Error saving product: ${e.message}"
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            }
        }
    }

    private fun resetForm() {
        productId = IdGenerator.generateProductId()
        tempImageFile = null
        tempImageUri = null
        _uiState.value = AddProductUiState(shouldClearForm = true)
    }

    fun onBackPressed() {
        _navigationEvent.value = NavigationEvent.NavigateBack
    }

    fun createTempImageFile(context: Context): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "temp_${UUID.randomUUID()}.jpg"
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up temp files when ViewModel is destroyed
        tempImageFile?.let { file ->
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