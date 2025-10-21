package com.example.data.local.sharedPrefs

import android.content.Context
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.util.Constants.OWNER_ROLE

class SharedPref(context: Context){
    private val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()!!

    fun setLogin(){
        editor.putBoolean("isLogin", true)
        editor.apply()
    }
    fun isLogin():Boolean{
        return sharedPref.getBoolean("isLogin", false)
    }
    fun setRole(role:Int){
        editor.putInt("userRole", role)
        editor.apply()
    }
    fun getRole():Int{
        return sharedPref.getInt("userRole", OWNER_ROLE)
    }

    fun saveUser(id: String,
                 name: String,
                 role: Int,
                 email: String,
                 password: String ){
        editor.putString("userId", id)
        editor.putString("userName", name)
        editor.putInt("userRole", role)
        editor.putString("userEmail", email)
        editor.putString("userPassword",password)
        editor.apply()

    }
    fun getUser(): User{
        val id = sharedPref.getString("userId", "")!!
        val name = sharedPref.getString("userName", "")!!
        val role = sharedPref.getInt("userRole", OWNER_ROLE)
        val email = sharedPref.getString("userEmail", "")!!
        val password = sharedPref.getString("userPassword", "")!!
        return User(id, name, role, email, password)

    }

    fun saveStore(id: String, name: String, phone: String, location:String){
        editor.putString("storeId",id)
        editor.putString("storeName",name)
        editor.putString("storePhone",phone)
        editor.putString("storeLocation",location)
        editor.apply()

    }

    fun getStore():Store{
        val id = sharedPref.getString("storeId", "")!!
        val name = sharedPref.getString("storeName", "")!!
        val phone = sharedPref.getString("storePhone", "")!!
        val location = sharedPref.getString("storeLocation", "")!!
        return Store(id =id, name = name, location =location, phone = phone)
    }

    fun clearPrefs(){
        editor.clear()
        editor.apply()
    }


}