package com.example.domain.repo

interface AuthRepo{

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
    fun createInvite(storeId: String, email:String,onResult:(success:Boolean,msg:String)->Unit)
    fun registerEmployee(name: String, email:String, password:String, code:String,onResult:(success:Boolean,msg:String)->Unit)
    fun approveEmployee(uid:String,onResult:(success:Boolean,msg:String)->Unit)
    fun rejectEmployee(uid:String,onResult:(success:Boolean,msg:String)->Unit)
    fun logout(onResult: (Boolean, String) -> Unit)
}