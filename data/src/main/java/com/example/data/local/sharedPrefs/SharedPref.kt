package com.example.data.local.sharedPrefs

import android.content.Context
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_PENDING

class SharedPref(context: Context){
    private val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()!!


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


    fun setUserName(newName:String){
        editor.putString("userName",newName)
        editor.apply()

    }
    fun setEmail(newEmail:String){
        editor.putString("userEmail",newEmail)
        editor.apply()
    }

    fun setProfileImage(profileUrl:String){
        editor.putString("profileUrl",profileUrl)
        editor.apply()
    }
    fun getProfileImage():String{
        return sharedPref.getString("profileUrl", "")!!
    }

    fun saveUser(user:User){
        editor.putString("profileUrl",user.photoUrl)
        editor.putString("userId", user.id)
        editor.putString("userName", user.name)
        editor.putInt("userRole", user.role)
        editor.putString("userEmail", user.email)
        editor.putBoolean("isLogin", true)
        editor.putString("userStatus", user.status)
        editor.putString("storeId",user.storeId)
        editor.apply()

    }
    fun getUser(): User{
        val id = sharedPref.getString("userId", "")!!
        val name = sharedPref.getString("userName", "")!!
        val role = sharedPref.getInt("userRole", OWNER_ROLE)
        val email = sharedPref.getString("userEmail", "")!!
        val profileUrl = sharedPref.getString("profileUrl", "")!!
        val status = sharedPref.getString("userStatus",STATUS_PENDING)!!
        val storeId = sharedPref.getString("storeId", "")!!
        return User(
            id =id,
            name =name,
            role= role,
            email=email,
            photoUrl = profileUrl,
            status = status,
            storeId = storeId)
    }

    fun saveStore(store: Store){
        editor.putString("storeId",store.id)
        editor.putString("storeName",store.name)
        editor.putString("storePhone",store.phone)
        editor.putString("storeLocation",store.location)
        editor.putString("ownerId",store.ownerId)
        editor.apply()

    }

    fun getStore():Store{
        val id = sharedPref.getString("storeId", "")!!
        val name = sharedPref.getString("storeName", "")!!
        val phone = sharedPref.getString("storePhone", "")!!
        val location = sharedPref.getString("storeLocation", "")!!
        val ownerId = sharedPref.getString("ownerId", "")!!
        return Store(id =id, name = name, location =location, phone = phone, ownerId =ownerId)
    }

    fun clearPrefs(){
        editor.clear().apply()
    }
    fun setLoginFromGoogle(){
        editor.putBoolean("isLoginFromGoogle", true)
        editor.apply()
    }
    fun isLoginFromGoogle():Boolean{
        return sharedPref.getBoolean("isLoginFromGoogle", false)

    }

}