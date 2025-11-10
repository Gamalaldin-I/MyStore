package com.example.htopstore.ui.main

import android.net.Uri
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
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetArchiveSizeUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.profile.ChangeProfileImageUseCase
import com.example.domain.useCase.profile.RemoveProfileImageUseCase
import com.example.domain.useCase.profile.UpdateNameUseCase
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
    private val productRepo:ProductRepo,
    private val changeProfileImageUseCase:ChangeProfileImageUseCase,
    private val removeProfileImageUseCase: RemoveProfileImageUseCase

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
        viewModelScope.launch{
            val (success, msg) = logoutUseCase()
            if(success){
                pref.clearPrefs()
                withContext(Dispatchers.IO){db.clearAllTables()}
            }
            onResult(success, msg)
            _message.postValue(msg)
    }
    }


    //val employeeStatus = authRepo.employeeStatus.asLiveData()

    fun startListening(){
        if(pref.getRole()!= Constants.OWNER_ROLE){
        //authRepo.listenToEmployee()
        }
    }
    fun startListenForProducts(){
      //  productRepo.listenToRemoteChanges(viewModelScope)
    }



    fun updateName(name: String, onView: () -> Unit) {
        viewModelScope.launch{
           val  (success, msg) = withContext(Dispatchers.IO){updateNameUseCase(name)}
                if(success) onView()
                _message.postValue(msg)
            }
    }


    fun isLoginFromGoogle(): Boolean{
        return pref.isLoginFromGoogle()
    }
    fun getProfileImage():String{
        return pref.getProfileImage().toString()
    }
    fun changePhoto(uri:Uri,onResult: () -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            changeProfileImageUseCase(uri){
                success, msg ->
                _message.postValue(msg)
            }
        }
    }
    fun removeProfilePhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            removeProfileImageUseCase { success, msg ->
                _message.postValue(msg)
            }
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




}