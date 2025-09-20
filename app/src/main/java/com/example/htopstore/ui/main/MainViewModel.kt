package com.example.htopstore.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CartProduct
import com.example.domain.model.Product
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.product.GetLowStockUseCase
import com.example.domain.useCase.product.GetTop5UseCase
import com.example.domain.useCase.sales.SellUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTop5InSalesUseCase: GetTop5UseCase,
    private val getLowStockUseCase: GetLowStockUseCase,
    private  val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val getArchiveProductsUseCase: GetArchiveProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val sellUseCase: SellUseCase

): ViewModel(){

    private val _top5 = MutableLiveData<List<Product>>()
    val top5: LiveData<List<Product>> = _top5

    private val _lowStock = MutableLiveData<List<Product>>()
    val lowStock: LiveData<List<Product>> = _lowStock

    private  val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private  val _archive = MutableLiveData<List<Product>>()
    val archive: LiveData<List<Product>> = _archive


    fun getTop5InSales() {
        viewModelScope.launch(Dispatchers.IO) {
            val products = getTop5InSalesUseCase()
            _top5.postValue( products)
        }
    }
    fun getLowStock(){
        viewModelScope.launch(Dispatchers.IO) {
            val products = getLowStockUseCase()
            _lowStock.postValue( products)
        }
    }
    fun getStockProducts() =
        viewModelScope.launch(Dispatchers.IO){
            _products.postValue(getAvailableProductsUseCase.invoke())
        }

    fun getArchiveProducts() =
        viewModelScope.launch(Dispatchers.IO){
            _archive.postValue(getArchiveProductsUseCase.invoke()) }

    fun deleteProduct(id:String, image:String,onFinish:()->Unit) =
        viewModelScope.launch(Dispatchers.IO){
            deleteProductUseCase(id,image)
            withContext(Dispatchers.Main){
                onFinish()
            }
        }
    fun sell(cartList: List<CartProduct>, discount: Int = 0,onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            sellUseCase(cartList, discount)
            withContext(Dispatchers.Main){
                onFinish()
            }
        }
    }
}