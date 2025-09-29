package com.example.htopstore.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategorySales
import com.example.domain.model.Product
import com.example.domain.useCase.analisys.product.GetHaveNotSoldProductsUseCase
import com.example.domain.useCase.analisys.product.GetReturningCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetSellingCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetTheHighestProfitProductsUseCase
import com.example.domain.useCase.analisys.product.GetTheLeastSellingCategoryUseCase
import com.example.domain.useCase.analisys.product.GetTheMostSellingCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel
    @Inject constructor(
        //for products analysis
        private val getTheLeastSellingCategoryUseCase: GetTheLeastSellingCategoryUseCase,
        private val getTheMostSellingCategoryUseCase: GetTheMostSellingCategoryUseCase,
        private val getReturningCategoriesUseCase: GetReturningCategoriesUseCase,
        private val getSellingCategoriesUseCase: GetSellingCategoriesUseCase,
        private val getTheHighestProfitProductsUseCase: GetTheHighestProfitProductsUseCase,
        private val getHaveNotSoldProductsUseCase: GetHaveNotSoldProductsUseCase
    ): ViewModel() {

        // for products analysis
        private val _sellingCategories = MutableLiveData<List<CategorySales>>(emptyList())
        private val _returningCategories = MutableLiveData<List<CategorySales>>(emptyList())
        private val _theLeastSellingCategory = MutableLiveData<String>("")
        private val _theMostSellingCategory = MutableLiveData<String>("")
        private val _theHighestProfitProducts = MutableLiveData<List<Product>>(emptyList())
        private val _haveNotSoldProducts = MutableLiveData<List<Product>>(emptyList())

        val sellingCategory: LiveData<List<CategorySales>> = _sellingCategories
        val returningCategories: LiveData<List<CategorySales>> = _returningCategories
        val theLeastSellingCategory: LiveData<String> = _theLeastSellingCategory
        val theMostSellingCategory: LiveData<String> = _theMostSellingCategory
        val theHighestProfitProducts: LiveData<List<Product>> = _theHighestProfitProducts
        val haveNotSoldProducts: LiveData<List<Product>> = _haveNotSoldProducts


    private fun getTheLeastSellingCategory(duration: String) {
        viewModelScope.launch(Dispatchers.IO){
               _theLeastSellingCategory.postValue(getTheLeastSellingCategoryUseCase(duration))
            }
        }
    private fun getTheMostSellingCategory(duration: String) {
        viewModelScope.launch(Dispatchers.IO){
            _theMostSellingCategory.postValue(getTheMostSellingCategoryUseCase(duration))
        }
    }
    private fun getReturningCategories(duration: String) {
        viewModelScope.launch(Dispatchers.IO){
            _returningCategories.postValue(getReturningCategoriesUseCase(duration))
        }
    }

    private fun getSellingCategories(duration: String) {
        viewModelScope.launch(Dispatchers.IO){
            _sellingCategories.postValue(getSellingCategoriesUseCase(duration))
        }
    }

     fun getTheHighestProfitProducts() {
        viewModelScope.launch(Dispatchers.IO){
            _theHighestProfitProducts.postValue(getTheHighestProfitProductsUseCase())
        }
    }

     fun getHaveNotSoldProducts() {
        viewModelScope.launch(Dispatchers.IO){
            _haveNotSoldProducts.postValue(getHaveNotSoldProductsUseCase())
        }
    }
    fun getProductsAnalysis(duration: String){
        getTheLeastSellingCategory(duration)
        getTheMostSellingCategory(duration)
        getReturningCategories(duration)
        getSellingCategories(duration)
    }






}