package com.example.htopstore.ui.product

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.local.roomDb.AppDataBase
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Product
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.useCase.product.UpdateProductImageUseCase
import com.example.domain.useCase.product.UpdateProductUseCase
import com.example.domain.util.CartHelper
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
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
    private val updateProductImageUseCase: UpdateProductImageUseCase,
    private val app: Application,
    private val logoutUseCase: LogoutUseCase,
    private val db: AppDataBase,
    private val pref: SharedPref
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

    fun updateProduct(newProductData: Product, onFiredAction: () -> Unit, onFinish: () -> Unit) {
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

        val updatedProduct = newProductData.copy(
            lastUpdate = DateHelper.getCurrentTimestampTz()
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (bool, msg) = updateProductUseCase(updatedProduct)
                if (bool) {
                    _message.postValue(app.getString(R.string.product_updated_successfully))
                    withContext(Dispatchers.Main) { onFinish() }
                } else {
                    if (msg == Constants.STATUS_FIRED) {
                        onFiredAction()
                    } else {
                        _message.postValue(app.getString(R.string.error_updating_product))
                    }
                }
            } catch (e: Exception) {
                _message.postValue(app.getString(R.string.error_updating_product, e.message ?: ""))
            }
        }
    }
    fun updateProductImage(
        uri: Uri,
        productId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (success, result) = updateProductImageUseCase(uri, productId)
                withContext(Dispatchers.Main) {
                    if (success) {
                        onSuccess(result)
                    } else {
                        onError(result)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: app.getString(R.string.error_uploading_image))
                }
            }
        }
    }

    fun deleteProduct(product: Product, onFiredAction: () -> Unit, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (success, msg) = deleteProductUseCase(product.id, product.productImage)
                withContext(Dispatchers.Main) {
                    if (success) {
                        CartHelper.removeFromTheCartList(product.id)
                        onFinish()
                    } else {
                        if (msg == Constants.STATUS_FIRED) {
                            onFiredAction()
                        }
                    }
                }
                _message.postValue(msg)
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

    fun logout(onResult: () -> Unit) {
        viewModelScope.launch {
            val (success, msg) = logoutUseCase()
            if (success) {
                pref.clearPrefs()
                withContext(Dispatchers.IO) { db.clearAllTables() }
                withContext(Dispatchers.Main) {
                    onResult()
                }
            } else {
                _message.postValue(msg)
            }
        }
    }
}