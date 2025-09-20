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
class ProductViewModel
    @Inject constructor(
    private val getProductUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel(){

    private var _product = MutableLiveData<Product?>()
    val product : LiveData<Product?> = _product

    private var _message = MutableLiveData<String>()
    val message : LiveData<String> = _message


    fun getProduct(id:String){
        viewModelScope.launch(Dispatchers.IO){
            _product.postValue(getProductUseCase(id))
        }
    }

    private fun allIsDone(type: String, name: String,  purchase: String, sell: String, count: String): Boolean{
            if (type.isEmpty()) {
                _message.value = "Please select a type"
                return false
            }
            if (name.isEmpty()) {
                _message.value = "Please enter a brand"
                return false
            }
            if (purchase.isEmpty()) {
                _message.value="Please enter a buying price"
                return false
            }
            if (sell.isEmpty()) {
                _message.value = "Please enter a selling price"
                return false
            }
            if (count.isEmpty()) {
                _message.value = "Please enter a count"
                return false
            }
            if (count.toInt() <= 0) {
                _message.value ="Invalid count"
                return false
            }
            if (purchase.toDouble() <= 0) {
                _message.value ="Invalid buying price"
                return false
            }
            if (sell.toDouble() <= 0) {
                _message.value ="Invalid selling price"
                return false
            }
            if (sell.toDouble() <=
                purchase.toDouble()
            ) {
                _message.value ="Selling price must be > buying price"
                return false
            }
            return true
        }

     fun updateProduct(newProductData: Product, onFinish:()->Unit){
        if(!allIsDone(
            name = newProductData.name,
            type = newProductData.category,
            count = newProductData.count.toString(),
            purchase = newProductData.buyingPrice.toString(),
            sell = newProductData.sellingPrice.toString()
        )) return
        viewModelScope.launch(Dispatchers.IO){
        updateProductUseCase(newProductData)

        withContext(Dispatchers.Main){
            onFinish()
        }
        }
    }
     fun deleteProduct(product: Product,onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteProductUseCase(product.id, product.productImage)
            withContext(Dispatchers.Main) {
                CartHelper.removeFromTheCartList(product.id)
                onFinish()
            }
        }
    }

}