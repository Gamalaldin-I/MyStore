package com.example.htopstore.ui.bills

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Bill
import com.example.domain.useCase.bill.GetAllBillsUseCase
import com.example.domain.useCase.bill.GetBillByDateUseCase
import com.example.domain.useCase.bill.GetBillsByDateRangeUseCase
import com.example.domain.useCase.bill.GetBillsTillDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val getAllBillsUseCase: GetAllBillsUseCase,
    private val getBillsTillDateUseCase: GetBillsTillDateUseCase,
    private val getBillByDateUseCase: GetBillByDateUseCase,
    private val getBillsByDateRangeUseCase: GetBillsByDateRangeUseCase
) : ViewModel() {

    private val dbFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

    // LiveData for bills list
    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> = _bills

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData for total sum
    private val _totalSum = MutableLiveData<Double>()
    val totalSum: LiveData<Double> = _totalSum

    // Current filter state
    private var currentRequestCode = ALL_BILLS_REQ
    private var sinceDate: LocalDate? = null
    private var toDate: LocalDate? = null

    companion object {
        const val ALL_BILLS_REQ = 0
        const val FILTERED_BY_DATE_BILLS_REQ = 1
        const val FILTERED_BY_DATE_RANGE_BILLS_REQ = 2
        const val FILTERED_BY_DATE_TILL_DATE_BILLS_REQ = 3
    }

    init {
        // Load all bills initially
        getBillsByRequestCode(ALL_BILLS_REQ)
    }

    fun refreshBills() {
        getBillsByRequestCode(currentRequestCode)
    }

    fun setSinceDate(date: LocalDate?): String? {
        return if (toDate != null && date != null && date.isAfter(toDate)) {
            "Since date must be before To date"
        } else {
            sinceDate = date
            getBillsByRequestCode(getUseCaseCode())
            null
        }
    }

    fun setToDate(date: LocalDate?): String? {
        return if (sinceDate != null && date != null && date.isBefore(sinceDate)) {
            "To date must be after Since date"
        } else {
            toDate = date
            getBillsByRequestCode(getUseCaseCode())
            null
        }
    }

    fun resetFilters() {
        sinceDate = null
        toDate = null
        getBillsByRequestCode(ALL_BILLS_REQ)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun getUseCaseCode() = when {
        sinceDate == null && toDate == null -> ALL_BILLS_REQ
        sinceDate != null && toDate == null -> FILTERED_BY_DATE_BILLS_REQ
        sinceDate != null && toDate != null -> FILTERED_BY_DATE_RANGE_BILLS_REQ
        else -> FILTERED_BY_DATE_TILL_DATE_BILLS_REQ
    }

    private fun getBillsByRequestCode(requestCode: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val billsList = when (requestCode) {
                    ALL_BILLS_REQ -> getAllBillsUseCase()
                    FILTERED_BY_DATE_BILLS_REQ -> sinceDate?.let {
                        getBillByDateUseCase(it.format(dbFormatter))
                    } ?: emptyList()
                    FILTERED_BY_DATE_RANGE_BILLS_REQ -> if (sinceDate != null && toDate != null) {
                        getBillsByDateRangeUseCase(
                            sinceDate!!.format(dbFormatter),
                            toDate!!.format(dbFormatter)
                        )
                    } else emptyList()
                    FILTERED_BY_DATE_TILL_DATE_BILLS_REQ -> toDate?.let {
                        getBillsTillDateUseCase(it.format(dbFormatter))
                    } ?: emptyList()
                    else -> emptyList()
                }

                currentRequestCode = requestCode
                _bills.postValue(billsList)
                _totalSum.postValue(calculateTotalSum(billsList))
                _isLoading.postValue(false)

            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
                _bills.postValue(emptyList())
                _totalSum.postValue(0.0)
                _isLoading.postValue(false)
            }
        }
    }

    private fun calculateTotalSum(bills: List<Bill>): Double {
        return bills.sumOf { it.totalCash }
    }
}