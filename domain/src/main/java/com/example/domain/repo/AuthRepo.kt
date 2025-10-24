package com.example.domain.repo

import kotlinx.coroutines.flow.StateFlow

interface AuthRepo{

    val employeeStatus: StateFlow<String>

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

    fun registerEmployee(name: String,
                         email:String,
                         password:String,
                         onResult:(success:Boolean,msg:String)->Unit)

    fun logout(onResult: (Boolean, String) -> Unit)
}