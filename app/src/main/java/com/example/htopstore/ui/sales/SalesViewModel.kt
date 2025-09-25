package com.example.htopstore.ui.sales
import androidx.lifecycle.ViewModel
import com.example.domain.useCase.sales.GetAllSalesAndReturnsByDateUseCase
import com.example.domain.useCase.sales.GetAllSalesAndReturnsUseCase
import com.example.domain.useCase.sales.GetReturnsByDateUseCase
import com.example.domain.useCase.sales.GetReturnsUseCase
import com.example.domain.useCase.sales.GetSoldOnlyByDateUseCase
import com.example.domain.useCase.sales.GetSoldOnlyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val getSoldOnlyUseCase: GetSoldOnlyUseCase,
    private val getReturnsUseCase: GetReturnsUseCase,
    private val getSoldOnlyByDateUseCase: GetSoldOnlyByDateUseCase,
    private val getReturnsByDateUseCase: GetReturnsByDateUseCase,
    private val getAllSalesAndReturnsUseCase: GetAllSalesAndReturnsUseCase,
    private val getAllSalesAndReturnsByDateUseCase: GetAllSalesAndReturnsByDateUseCase
) : ViewModel() {

   /* // LiveData for sales data
    private val _salesData = MutableLiveData<List<SoldProduct>>()
    val salesData: LiveData<List<SoldProduct>> = _salesData

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData for current filter state
    private val _currentFilter = MutableLiveData<SalesFilter>()
    val currentFilter: LiveData<SalesFilter> = _currentFilter

    // LiveData for item count
    private val _itemCount = MutableLiveData<Int>()
    val itemCount: LiveData<Int> = _itemCount

    // Current state
    private var isFiltered = false
    private var selectedDate: String = ""
    private var selectedType: SalesType = SalesType.ALL_SALES

    companion object {
        enum class SalesType(val value: Int) {
            ALL_SALES(0),
            SOLD(1),
            RETURNS(2)
        }

        data class SalesFilter(
            val type: SalesType,
            val isFiltered: Boolean,
            val date: String
        )
    }

    init {
        // Load initial data
        loadSalesData(SalesType.ALL_SALES)
    }

    fun loadSalesData(salesType: SalesType, date: String? = null) {
        selectedType = salesType

        if (!date.isNullOrEmpty()) {
            selectedDate = date
            isFiltered = true
            loadFilteredData(salesType, date)
        } else {
            isFiltered = false
            selectedDate = ""
            loadAllData(salesType)
        }

        updateCurrentFilter()
    }

    fun refreshCurrentData() {
        if (isFiltered) {
            loadFilteredData(selectedType, selectedDate)
        } else {
            loadAllData(selectedType)
        }
    }

    fun resetFilters() {
        isFiltered = false
        selectedDate = ""
        loadAllData(selectedType)
        updateCurrentFilter()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadAllData(salesType: SalesType) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: List<SoldProduct> = when (salesType) {
                    SalesType.ALL_SALES -> getAllSalesAndReturnsUseCase()
                    SalesType.SOLD -> getSoldOnlyUseCase()
                    SalesType.RETURNS -> getReturnsUseCase()
                }

                _salesData.postValue(list)
                _itemCount.postValue(list.size)
                _isLoading.postValue(false)

            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load sales data: ${e.message}")
                _salesData.postValue(emptyList())
                _itemCount.postValue(0)
                _isLoading.postValue(false)
            }
        }
    }

    private fun loadFilteredData(salesType: SalesType, date: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: List<SoldProduct> = when (salesType) {
                    SalesType.ALL_SALES -> getAllSalesAndReturnsByDateUseCase(date)
                    SalesType.SOLD -> getSoldOnlyByDateUseCase(date)
                    SalesType.RETURNS -> getReturnsByDateUseCase(date)
                }

                _salesData.postValue(list)
                _itemCount.postValue(list.size)
                _isLoading.postValue(false)

            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load filtered sales data: ${e.message}")
                _salesData.postValue(emptyList())
                _itemCount.postValue(0)
                _isLoading.postValue(false)
            }
        }
    }

    private fun updateCurrentFilter() {
        _currentFilter.value = SalesFilter(
            type = selectedType,
            isFiltered = isFiltered,
            date = selectedDate
        )
    }

    fun validateProductNavigation(soldProduct: SoldProduct): ValidationResult {
        return if (soldProduct.productId == null) {
            ValidationResult.Error("The product is not available now")
        } else {
            ValidationResult.Success
        }
    }

    fun validateBillNavigation(soldProduct: SoldProduct): ValidationResult {
        return if (soldProduct.saleId == null) {
            ValidationResult.Error("The product has not been sold")
        } else {
            ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }*/
}