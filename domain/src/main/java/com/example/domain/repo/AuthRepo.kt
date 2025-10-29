package com.example.domain.repo

import kotlinx.coroutines.flow.StateFlow

interface AuthRepo{

    val employeeStatus: StateFlow<String>
    //login,logout and register
    fun login(email:String,
              password:String,
              onResult:(success:Boolean,msg:String)->Unit
    )
    fun registerOwner(
        email:String,
        password:String,
        name:String,
        storeName: String,
        storeLocation: String,
        storePhone: String,
        onResult:(success:Boolean,msg:String)->Unit
    )
    fun listenToEmployee()
    fun stopListening()

    fun registerEmployee(
        name: String,
        email:String,
        password:String,
        onResult:(success:Boolean,msg:String)->Unit)
    fun logout(onResult: (Boolean, String) -> Unit)


    //account updates
    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit)
    fun changePassword(oldPassword:String,newPassword:String,onResult: (Boolean, String) -> Unit)
    fun updateName(name:String,onResult: (Boolean, String) -> Unit)
    fun updateEmail(newEmail:String,password: String,onResult: (Boolean, String) -> Unit)
}