package com.example.htopstore.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategorySales
import com.example.domain.model.ExpensesWithCategory
import com.example.domain.model.Product
import com.example.domain.model.SalesProfitByPeriod
import com.example.domain.useCase.analisys.product.GetHaveNotSoldProductsUseCase
import com.example.domain.useCase.analisys.product.GetReturningCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetSellingCategoriesUseCase
import com.example.domain.useCase.analisys.product.GetTheHighestProfitProductsUseCase
import com.example.domain.useCase.analisys.product.GetTheLeastSellingCategoryUseCase
import com.example.domain.useCase.analisys.product.GetTheMostSellingCategoryUseCase
import com.example.domain.useCase.analisys.sales.GetExpensesWithCategoryUseCase
import com.example.domain.useCase.analisys.sales.GetProfitByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetSalesAndProfitByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheAVGSalesUseCase
import com.example.domain.useCase.analisys.sales.GetTheMostSellingDayByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheMostSellingHourByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheNumOfSalesOpsUseCase
import com.example.domain.useCase.analisys.sales.GetTheTotalSalesUseCase
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
        private val getHaveNotSoldProductsUseCase: GetHaveNotSoldProductsUseCase,
        //for sales analysis
        private val getSalesAndProfitByPeriodUseCase: GetSalesAndProfitByPeriodUseCase,
        private val getTheAVGSalesUseCase: GetTheAVGSalesUseCase,
        private val getTheNumOfSalesOpsUseCase: GetTheNumOfSalesOpsUseCase,
        private val getTheTotalSalesUseCase: GetTheTotalSalesUseCase,
        private val getTheHoursWithHighestSalesUseCase: GetTheMostSellingHourByPeriodUseCase,
        private val getTheDaysWithHighestSalesUseCase: GetTheMostSellingDayByPeriodUseCase,
        private val getProfitByPeriod: GetProfitByPeriodUseCase,
        //for expenses
        private val getExpensesWithCategoryUseCase: GetExpensesWithCategoryUseCase

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


    //for sales analysis


    private val _salesAndProfitByPeriod = MutableLiveData<List<SalesProfitByPeriod>>(emptyList())
    private val _avgOfSales = MutableLiveData<Double>(0.0)
    private val _numberOfSales = MutableLiveData<Int>(0)
    private val _totalSales = MutableLiveData<Double?>(0.0)
    private val _theHoursWithHighestSales = MutableLiveData<String>()
    private val _theDaysWithHighestSales = MutableLiveData<String>()
    private val _profit = MutableLiveData<Double>(0.0)

    val salesAndProfitByPeriod: LiveData<List<SalesProfitByPeriod>> = _salesAndProfitByPeriod
    val avgOfSales: LiveData<Double> = _avgOfSales
    val numberOfSales: LiveData<Int> = _numberOfSales
    val totalSales: LiveData<Double?> = _totalSales
    val theHoursWithHighestSales: LiveData<String> = _theHoursWithHighestSales
    val theDaysWithHighestSales: LiveData<String> = _theDaysWithHighestSales
    val profit: LiveData<Double> = _profit



    fun getSalesAndProfitByPeriod(period: String) {
        viewModelScope.launch(Dispatchers.IO){
            _salesAndProfitByPeriod.postValue(getSalesAndProfitByPeriodUseCase(period))
        }
    }
    fun getTheAVGSales(period: String){
        viewModelScope.launch(Dispatchers.IO) {
            _avgOfSales.postValue(getTheAVGSalesUseCase(period))
        }
    }
    fun getTheNumOfSales(period: String){
        viewModelScope.launch(Dispatchers.IO) {
            _numberOfSales.postValue(getTheNumOfSalesOpsUseCase(period))
    }
    }
    fun getTheTotalOfSalesValue(period: String){
        viewModelScope.launch(Dispatchers.IO) {
            _totalSales.postValue(getTheTotalSalesUseCase(period))
        }
    }
    fun getTheProfit(period: String){
        viewModelScope.launch(Dispatchers.IO) {
            _profit.postValue(getProfitByPeriod(period))
        }
    }

    fun getTheBestPeriod(period: String){
        viewModelScope.launch(Dispatchers.IO){
            _theHoursWithHighestSales.postValue(getTheHoursWithHighestSalesUseCase(period))
        }
    }
    fun getTheBestDayOfWeek(period: String){
        viewModelScope.launch(Dispatchers.IO){
            _theDaysWithHighestSales.postValue(getTheDaysWithHighestSalesUseCase(period))
        }
    }


    //for expensesAnalysis


    private val _expensesWithCat = MutableLiveData<List<ExpensesWithCategory>>()
    val expensesWithCategory: LiveData<List<ExpensesWithCategory>> = _expensesWithCat

    fun getExpensesWithCategory(duration:String){
        viewModelScope.launch(Dispatchers.IO){
            _expensesWithCat.postValue(getExpensesWithCategoryUseCase(duration))
        }
    }

}