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
import com.example.domain.repo.ProductRepo
import com.example.domain.useCase.analisys.GetProfitByDayUseCase
import com.example.domain.useCase.analisys.GetTotalExpensesByDateUseCase
import com.example.domain.useCase.analisys.GetTotalSalesByDateUseCase
import com.example.domain.useCase.analisys.product.GetLowStockUseCase
import com.example.domain.useCase.analisys.product.GetTop5UseCase
import com.example.domain.useCase.auth.LogoutUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.sales.SellUseCase
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import com.example.htopstore.R
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
    private val sellUseCase: SellUseCase,
    getProfitByDayUseCase: GetProfitByDayUseCase,
    getTotalExpensesByDateUseCase: GetTotalExpensesByDateUseCase,
    getTotalSalesByDateUseCase: GetTotalSalesByDateUseCase,
    private val productRepo:ProductRepo,
    private val logoutUseCase:LogoutUseCase,
    private val db: AppDataBase,
    private val pref: SharedPref

): ViewModel(){
    val r = pref.getRole()
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    val todayDate = DateHelper.getCurrentDate()


    val top5: LiveData<List<Product>> = getTop5InSalesUseCase().asLiveData()

    val lowStock: LiveData<List<Product>> = getLowStockUseCase().asLiveData()

    val products: LiveData<List<Product>> = getAvailableProductsUseCase().asLiveData()

    val profit: LiveData<Double?> = getProfitByDayUseCase(todayDate).asLiveData()

    val totalExpenses: LiveData<Double?> = getTotalExpensesByDateUseCase(todayDate).asLiveData()

    val totalSales: LiveData<Double?> = getTotalSalesByDateUseCase(todayDate).asLiveData()


    fun sell(cartList: List<CartProduct>,discount: Int = 0, onProgress :(Float)->Unit,
             onFiredAction:()->Unit,
             onFinish: (msg:String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val msg = sellUseCase(cartList = cartList,
                        discount =discount){
                progress->
                onProgress(progress)

            }
            withContext(Dispatchers.Main){
                if(msg== Constants.STATUS_FIRED){
                    onFiredAction()
                }
                onFinish(msg)
            }
        }
    }


    fun fetchProductsFromRemote(){
        viewModelScope.launch{
         productRepo.fetchProductsFromRemoteIntoLocal()
        }
    }





    private val messageToStringRes = mapOf(
            "Error reading image" to R.string.error_reading_image,
            "Image uploaded successfully" to R.string.image_uploaded_successfully,
            "Error uploading image: %1\$s" to R.string.error_uploading_image,
            "Image removed successfully" to R.string.image_removed_successfully,
            "Error removing photo: %1\$s" to R.string.error_removing_photo,
            "Password reset email sent successfully" to R.string.password_reset_email_sent,
            "Error resetting password: %1\$s" to R.string.error_resetting_password,
            "Name updated successfully" to R.string.name_updated_successfully,
            "Error updating name: %1\$s" to R.string.error_updating_name,
            "The session of the user is not valid" to R.string.session_not_valid,
            "The current password is incorrect" to R.string.current_password_incorrect,
            "Email confirmation sent successfully. Please check your new email address." to R.string.email_confirmation_sent,
            "Error updating email: %1\$s" to R.string.error_updating_email
        )

    fun getStringResFromMessage(message: String): Int {
        return messageToStringRes[message]?: R.string.unknown_error
    }

    fun logout(onResult: () -> Unit){
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
                withContext(Dispatchers.IO){db.clearAllTables()}
                withContext(Dispatchers.Main){
                    onResult()
                }
            }else{
                _message.postValue(msg)
            }
        }
    }

}