package com.example.htopstore.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.useCase.product.UpdateProductUseCase
import com.example.domain.util.CartHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getProduct(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getProductUseCase(id)
                _product.postValue(result)
            } catch (e: Exception) {
                _message.postValue("Error loading product: ${e.message}")
            }
        }
    }

    fun updateProduct(newProductData: Product, onFinish: () -> Unit) {
        // Validate the product data
        val validationError = validateProductData(
            type = newProductData.category,
            name = newProductData.name,
            purchase = newProductData.buyingPrice,
            sell = newProductData.sellingPrice,
            count = newProductData.count
        )

        if (validationError != null) {
            _message.value = validationError
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateProductUseCase(newProductData)
                _message.postValue("Product updated successfully")
                withContext(Dispatchers.Main) {
                    onFinish()
                }
            } catch (e: Exception) {
                _message.postValue("Error updating product: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: Product, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteProductUseCase(product.id, product.productImage)
                withContext(Dispatchers.Main) {
                    CartHelper.removeFromTheCartList(product.id)
                    _message.value = "Product deleted successfully"
                    onFinish()
                }
            } catch (e: Exception) {
                _message.postValue("Error deleting product: ${e.message}")
            }
        }
    }

    private fun validateProductData(
        type: String,
        name: String,
        purchase: Double,
        sell: Double,
        count: Int
    ): String? {
        return when {
            type.isBlank() -> "Please select a type"
            name.isBlank() -> "Please enter a brand"
            count <= 0 -> "Invalid count"
            purchase <= 0 -> "Invalid buying price"
            sell <= 0 -> "Invalid selling price"
            sell <= purchase -> "Selling price must be greater than buying price"
            else -> null
        }
    }
}