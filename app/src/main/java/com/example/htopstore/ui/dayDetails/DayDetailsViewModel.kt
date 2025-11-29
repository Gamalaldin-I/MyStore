package com.example.htopstore.ui.dayDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Bill
import com.example.domain.model.Expense
import com.example.domain.model.SoldProduct
import com.example.domain.model.User
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import com.example.domain.useCase.bill.GetBillByDateUseCase
import com.example.domain.useCase.expenses.GetExpensesByDateUseCase
import com.example.domain.useCase.sales.GetReturnsByDateUseCase
import com.example.domain.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayDetailsViewModel
    @Inject constructor(
        private val pref: SharedPref,
        private val supabaseClient: SupabaseClient,
    private val getReturnsByDateUseCase: GetReturnsByDateUseCase,
    private val getBillsByDateUseCase: GetBillByDateUseCase,
    private val getTotalExpensesByDateUseCase: GetTotalExpensesByDateUseCase,
    private val getProfitByDayUseCase: GetProfitByDayUseCase,
    private val getTotalSalesByDateUseCase: GetTotalSalesByDateUseCase,
    private val getExpensesListByDateUseCase: GetExpensesByDateUseCase
    )
    : ViewModel() {
        private val  _loading = MutableLiveData<Boolean>()
        val loading: LiveData<Boolean> = _loading
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    private val _returns : MutableLiveData<List<SoldProduct>> = MutableLiveData()
    val returns: LiveData<List<SoldProduct>> = _returns
    private val _bills : MutableLiveData<List<Bill>> = MutableLiveData()
    val bills: LiveData<List<Bill>> =  _bills
    private val _expenses : MutableLiveData<Double?> = MutableLiveData()
    val expenses: LiveData<Double?> =  _expenses
    private val _profit : MutableLiveData<Double?> = MutableLiveData()
    val profit: LiveData<Double?> =  _profit
    private val _totalSales : MutableLiveData<Double?> = MutableLiveData()
    val totalSales: LiveData<Double?> =  _totalSales
    private val _expensesList : MutableLiveData<List<Expense>> = MutableLiveData()
    val expensesList: LiveData<List<Expense>> =  _expensesList


    fun getReturnsOfDay(date:String){
        viewModelScope.launch { getReturnsByDateUseCase(date).collect{
                _returns.value = it }
            }
        }
    fun getBillsOfDay(date:String){
        viewModelScope.launch { getBillsByDateUseCase(date).collect{
                _bills.value = it }
            }
        }
    fun getExpensesOfDay(date:String){
        viewModelScope.launch { getTotalExpensesByDateUseCase(date).collect{
                _expenses.value = it }
            }
        }
    fun getProfitOfDay(date:String){
        viewModelScope.launch { getProfitByDayUseCase(date).collect{
                _profit.value = it }
            }
        }
    fun getTotalSalesOfDay(date:String){
        viewModelScope.launch { getTotalSalesByDateUseCase(date).collect{
                _totalSales.value = it }
            }
        }
    fun getExpensesListByDate(date:String){
        viewModelScope.launch {
            _expensesList.postValue(getExpensesListByDateUseCase(date)) }
    }
    internal fun getEmployee(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId == pref.getUser().id) {
                    _user.postValue(pref.getUser())
                } else {
                    val user = supabaseClient.from("users").select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingle<User>()
                    _user.postValue(user)
                }
            } catch (e: Exception) {
                Log.e("BillDetViewModel", "Error fetching employee: ${e.message}")
                // Return default user if query fails
                _user.postValue(
                    User(
                        id = userId,
                        name = "Unknown Employee",
                        email = "",
                        role = 3,
                        photoUrl = "",
                        status = Constants.STATUS_FIRED,
                        storeId = "",
                        provider = ""
                    )
                )
            }
        }
    }




}