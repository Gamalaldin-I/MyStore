package com.example.htopstore.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CartProduct
import com.example.domain.model.Product
import com.example.domain.useCase.analisys.GetLowStockUseCase
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetTop5UseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetArchiveSizeUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.sales.SellUseCase
import com.example.domain.util.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getTop5InSalesUseCase: GetTop5UseCase,
    getLowStockUseCase: GetLowStockUseCase,
    getAvailableProductsUseCase: GetAvailableProductsUseCase,
    getArchiveProductsUseCase: GetArchiveProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val sellUseCase: SellUseCase,
    getArchiveSizeUseCase: GetArchiveSizeUseCase,
    getProfitByDayUseCase: GetProfitByDayUseCase,
    getTotalExpensesByDateUseCase: GetTotalExpensesByDateUseCase,
    getTotalSalesByDateUseCase: GetTotalSalesByDateUseCase

): ViewModel(){

    val todayDate = DateHelper.getCurrentDate()

    val archiveSize: LiveData<Int> = getArchiveSizeUseCase().asLiveData()

    val top5: LiveData<List<Product>> = getTop5InSalesUseCase().asLiveData()

    val lowStock: LiveData<List<Product>> = getLowStockUseCase().asLiveData()

    val products: LiveData<List<Product>> = getAvailableProductsUseCase().asLiveData()

    val archive: LiveData<List<Product>> = getArchiveProductsUseCase().asLiveData()

    val profit: LiveData<Double?> = getProfitByDayUseCase(todayDate).asLiveData()

    val totalExpenses: LiveData<Double?> = getTotalExpensesByDateUseCase(todayDate).asLiveData()

    val totalSales: LiveData<Double?> = getTotalSalesByDateUseCase(todayDate).asLiveData()


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