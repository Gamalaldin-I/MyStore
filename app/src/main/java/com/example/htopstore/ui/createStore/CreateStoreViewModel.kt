package com.example.htopstore.ui.createStore

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Plan
import com.example.domain.model.Store
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.store.AddCategoryUseCase
import com.example.domain.useCase.store.AddStoreUseCase
import com.example.domain.useCase.store.DeleteCategoryUseCase
import com.example.domain.useCase.store.UpdateStoreDataUseCase
import com.example.domain.util.IdGenerator
import com.example.htopstore.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateStoreViewModel
    @Inject constructor(
    private val app: Application,
    private val pref: SharedPref,
    private val addStoreUseCase: AddStoreUseCase,
    private val updateStoreUseCase: UpdateStoreDataUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val addNewCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
):AndroidViewModel(app){


    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    fun createStore(storeData: StoreData,plan:Plan, callback: (Boolean, String) -> Unit) {
        val newStore = Store(
            id = IdGenerator.generateTimestampedId(),
            name = storeData.name,
            location = storeData.address,
            phone = storeData.phone,
            plan = plan.name,
            logoUrl = storeData.logoUri.toString(),
            planProductLimit = plan.productsCount,
            planOperationLimit = plan.operationsCount,
            ownerId = pref.getUser().id ,
            productsCount = 0,
            operationsCount = 0,
            resetDate = "30/11",
            categories = ""
        )
        viewModelScope.launch(Dispatchers.IO){
            val result = addStoreUseCase(newStore)
            withContext(Dispatchers.Main){
            callback(result.first, result.second)
            }
        }
    }
    fun updateStore(
        storeData: StoreData,
        plan: Plan,
        callback: (Boolean, String) -> Unit
    ){
        val newStore = pref.getStore().copy(
            name = storeData.name,
            location = storeData.address,
            phone = storeData.phone,
            plan = plan.name,
            logoUrl = storeData.logoUri.toString(),
            planProductLimit = plan.productsCount,
            planOperationLimit = plan.operationsCount,
        )
        viewModelScope.launch {
            val result = updateStoreUseCase(newStore)
            withContext(Dispatchers.Main){
                callback(result.first, result.second)
            }
        }
    }

    fun getFreePlan():Plan{
        return Plan(
            name = app.getString(R.string.free_plan),
            description = "",
            price = 0.0,
            productsCount = 50,
            operationsCount = 100)
    }
    fun getSilverPlan():Plan{
        return Plan(
            name = app.getString(R.string.silver_plan),
            description = "",
            price = 19.0,
            productsCount = 200,
            operationsCount = 500)

    }
    fun getGoldPLan():Plan{
        return Plan(
            name = app.getString(R.string.gold_plan),
            description = "",
            price = 49.0,
            productsCount = 1000,
            operationsCount = 5000
        )
    }
    fun getPlatinumPlan():Plan{
        return Plan(
            name = app.getString(R.string.platinum),
            description = "",
            price = 99.0,
            productsCount = 2000,
            operationsCount = 10000
        )
    }
    fun validateToMain(goTo:()->Unit){
        if(pref.getUser().storeId.isNotEmpty()){
            goTo()
        }
    }

    fun getStore():Store{
        return pref.getStore()
    }


    fun logout(onResult: (Boolean, String) -> Unit){
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
            }
            onResult(success, msg)
            _message.postValue(msg)
        }
    }

    fun deleteCategory(cat:String,onDeleteView:(String)->Unit){
        viewModelScope.launch {
            val (success, msg) = deleteCategoryUseCase(cat)
            if(success){
                withContext(Dispatchers.Main) { onDeleteView(cat) }
            }
            _message.postValue(msg)
        }
    }
    fun addCategory(cat:String,onInsert:(String)->Unit){
        viewModelScope.launch {
            val (success, msg) = addNewCategoryUseCase(cat)
            if(success){
                withContext(Dispatchers.Main) { onInsert(cat) }
            }
            _message.postValue(msg)
        }
    }
    fun getCategories(): MutableList<String>{
        val list = pref.getStore().categories.split(",") as MutableList<String>
        Log.d("TAGLO", "getCategories: $list")
        return list
    }

}