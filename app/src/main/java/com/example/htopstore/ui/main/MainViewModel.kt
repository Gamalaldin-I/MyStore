package com.example.htopstore.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.local.roomDb.AppDataBase
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.CartProduct
import com.example.domain.model.Product
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.model.category.UserRoles
import com.example.domain.repo.AuthRepo
import com.example.domain.repo.ProductRepo
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import com.example.domain.useCase.analisys.product.GetLowStockUseCase
import com.example.domain.useCase.analisys.product.GetTop5UseCase
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.auth.UpdateNameUseCase
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetArchiveSizeUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.sales.SellUseCase
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val db: AppDataBase,
    private val pref: SharedPref,
    private val logoutUseCase: LogoutUseCase,
    private val updateNameUseCase: UpdateNameUseCase,
    getTop5InSalesUseCase: GetTop5UseCase,
    getLowStockUseCase: GetLowStockUseCase,
    getAvailableProductsUseCase: GetAvailableProductsUseCase,
    getArchiveProductsUseCase: GetArchiveProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val sellUseCase: SellUseCase,
    getArchiveSizeUseCase: GetArchiveSizeUseCase,
    getProfitByDayUseCase: GetProfitByDayUseCase,
    getTotalExpensesByDateUseCase: GetTotalExpensesByDateUseCase,
    getTotalSalesByDateUseCase: GetTotalSalesByDateUseCase,
    private val authRepo: AuthRepo,
    private val productRepo:ProductRepo

): ViewModel(){
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


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
    fun getUserData():User{
        return pref.getUser()
    }
    fun getStoreData():Store{
        return pref.getStore()
    }
    fun getRole():String? {
        val role = pref.getRole()
        return UserRoles.entries.find { it.role == role }?.roleName
    }

    fun logout(onResult: (Boolean, String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
                db.clearAllTables()
            withContext(Dispatchers.Main){
                pref.clearPrefs()
                logoutUseCase(onResult)
            }
        }
    }


    val employeeStatus = authRepo.employeeStatus.asLiveData()

    fun startListening(){
        if(pref.getRole()!= Constants.OWNER_ROLE){
        authRepo.listenToEmployee()
        }
    }
    fun startListenForProducts(){
        productRepo.listenToRemoteChanges()
    }
    fun clearStoreData(){
        pref.saveStore(
            id = "",
            name = "",
            phone = "",
            location = "",
            ownerId = "")
    }

    override fun onCleared() {
        super.onCleared()
        authRepo.stopListening()
        productRepo.stopListening()
    }
    fun updateName(name:String,onView: () -> Unit){
        updateNameUseCase(name){
                success, msg ->
            if(success){onView()}
            _message.value = msg
        }
    }
    fun isLoginFromGoogle(): Boolean{
        return pref.isLoginFromGoogle()
    }
    fun getProfileImage():String{
        return pref.getProfileImage().toString()
    }

}