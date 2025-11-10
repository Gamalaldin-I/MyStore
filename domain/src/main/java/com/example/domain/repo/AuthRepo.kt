package com.example.domain.repo

interface AuthRepo{

    //login,logout and register
    fun login(email:String,
              password:String,
              onResult:(success:Boolean,msg:String)->Unit
    )
    suspend fun signWithGoogle(
        idToken:String,
        role:Int,
        fromLoginScreen:Boolean
    ):Pair<Boolean,String>

    fun registerOwner(
        email:String,
        password:String,
        name:String,
        role:Int,
        onResult:(success:Boolean,msg:String)->Unit
    )


    suspend fun logout(): Pair<Boolean,String>


}