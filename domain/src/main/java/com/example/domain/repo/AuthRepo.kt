package com.example.domain.repo

interface AuthRepo{

    //login,logout and register
    fun login(email:String,
              password:String,
              onResult:(success:Boolean,msg:String)->Unit
    )
    fun signWithGoogle(
        idToken:String,
        role:Int,
        storePhone:String,
        storeName:String,
        storeLocation:String,
        onResult:(success:Boolean,msg:String)->Unit)

    fun registerOwner(
        email:String,
        password:String,
        name:String,
        storeName: String,
        storeLocation: String,
        storePhone: String,
        onResult:(success:Boolean,msg:String)->Unit
    )

    fun registerEmployee(
        name: String,
        email:String,
        password:String,
        onResult:(success:Boolean,msg:String)->Unit)


    fun logout(onResult: (Boolean, String) -> Unit)


}