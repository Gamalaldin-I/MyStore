package com.example.htopstore.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Product
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.useCase.product.UpdateProductUseCase
import com.example.domain.util.CartHelper
import com.example.htopstore.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val app: Application
) : AndroidViewModel(app) {

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
                _message.postValue(app.getString(R.string.error_loading_product, e.message ?: ""))
            }
        }
    }

    fun updateProduct(newProductData: Product, onFinish: () -> Unit) {
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
                _message.postValue(app.getString(R.string.product_updated_successfully))
                withContext(Dispatchers.Main) { onFinish() }
            } catch (e: Exception) {
                _message.postValue(app.getString(R.string.error_updating_product, e.message ?: ""))
            }
        }
    }

    fun deleteProduct(product: Product, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteProductUseCase(product.id, product.productImage)
                withContext(Dispatchers.Main) {
                    CartHelper.removeFromTheCartList(product.id)
                    _message.value = app.getString(R.string.product_deleted_successfully)
                    onFinish()
                }
            } catch (e: Exception) {
                _message.postValue(app.getString(R.string.error_deleting_product, e.message ?: ""))
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
            type.isBlank() -> app.getString(R.string.please_select_type)
            name.isBlank() -> app.getString(R.string.please_enter_brand)
            count <= 0 -> app.getString(R.string.invalid_count)
            purchase <= 0 -> app.getString(R.string.invalid_buying_price)
            sell <= 0 -> app.getString(R.string.invalid_selling_price)
            sell <= purchase -> app.getString(R.string.selling_price_must_be_greater)
            else -> null
        }
    }
}
