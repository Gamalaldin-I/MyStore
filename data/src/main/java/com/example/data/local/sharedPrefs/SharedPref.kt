package com.example.data.local.sharedPrefs

import android.content.Context
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.DateHelper

class SharedPref(context: Context) {

    private val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    /////////////////////////////////////////////////
    ////////////// Keys /////////////////////////////
    /////////////////////////////////////////////////
    private object Keys {
        const val IS_LOGIN = "isLogin"
        const val ROLE = "userRole"
        const val USER_NAME = "userName"
        const val USER_EMAIL = "userEmail"
        const val USER_PHOTO = "profileUrl"
        const val USER_ID = "userId"
        const val USER_STATUS = "userStatus"
        const val STORE_ID = "storeId"
        const val PROVIDER = "provider"
        const val IS_GOOGLE_LOGIN = "isLoginFromGoogle"

        const val STORE_NAME = "storeName"
        const val STORE_PHONE = "storePhone"
        const val STORE_LOCATION = "storeLocation"
        const val STORE_OWNER = "ownerId"
        const val STORE_PLAN = "plan"
        const val PLAN_PRODUCT_LIMIT = "planProductLimit"
        const val PLAN_OPERATION_LIMIT = "planOperationLimit"
        const val STORE_LOGO = "storeLogoUrl"
        const val RESET_DATE = "resetDate"

        const val LAST_PRODUCTS_UPDATE = "lastProductsUpdate"
        const val LAST_BILLS_UPDATE = "lastBillsUpdate"
        const val LAST_SALES_UPDATE = "lastSalesUpdate"
        const val LAST_EXPENSES_UPDATE = "lastExpensesUpdate"
    }

    /////////////////////////////////////////////////
    ////////////// Auth /////////////////////////////
    /////////////////////////////////////////////////
    fun isLogin(): Boolean =
        sharedPref.getBoolean(Keys.IS_LOGIN, false)

    fun setRole(role: Int) {
        editor.putInt(Keys.ROLE, role).apply()
    }

    fun getRole(): Int =
        sharedPref.getInt(Keys.ROLE, OWNER_ROLE)

    fun setLoginFromGoogle() {
        editor.putBoolean(Keys.IS_GOOGLE_LOGIN, true).apply()
    }

    fun isLoginFromGoogle(): Boolean =
        sharedPref.getBoolean(Keys.IS_GOOGLE_LOGIN, false)

    fun clearPrefs() {
        editor.clear().apply()
    }

    /////////////////////////////////////////////////
    ////////////// User /////////////////////////////
    /////////////////////////////////////////////////
    fun setUserName(name: String) {
        editor.putString(Keys.USER_NAME, name).apply()
    }

    fun getUserName(): String =
        sharedPref.getString(Keys.USER_NAME, "") ?: ""

    fun setEmail(email: String) {
        editor.putString(Keys.USER_EMAIL, email).apply()
    }

    fun setProfileImage(url: String) {
        editor.putString(Keys.USER_PHOTO, url).apply()
    }

    fun getProfileImage(): String =
        sharedPref.getString(Keys.USER_PHOTO, "") ?: ""

    fun saveUser(user: User) {
        editor.apply {
            putString(Keys.USER_PHOTO, user.photoUrl)
            putString(Keys.USER_ID, user.id)
            putString(Keys.USER_NAME, user.name)
            putInt(Keys.ROLE, user.role)
            putString(Keys.USER_EMAIL, user.email)
            putBoolean(Keys.IS_LOGIN, true)
            putString(Keys.USER_STATUS, user.status)
            putString(Keys.STORE_ID, user.storeId)
            putString(Keys.PROVIDER, user.provider)
        }.apply()
    }

    fun getUser(): User {
        return User(
            id = sharedPref.getString(Keys.USER_ID, "")!!,
            name = sharedPref.getString(Keys.USER_NAME, "")!!,
            role = sharedPref.getInt(Keys.ROLE, OWNER_ROLE),
            email = sharedPref.getString(Keys.USER_EMAIL, "")!!,
            photoUrl = sharedPref.getString(Keys.USER_PHOTO, "")!!,
            status = sharedPref.getString(Keys.USER_STATUS, STATUS_PENDING)!!,
            storeId = sharedPref.getString(Keys.STORE_ID, "")!!,
            provider = sharedPref.getString(Keys.PROVIDER, "")!!
        )
    }

    /////////////////////////////////////////////////
    ////////////// Store ////////////////////////////
    /////////////////////////////////////////////////
    fun setStoreLogoUrl(url: String) {
        editor.putString(Keys.STORE_LOGO, url).apply()
    }

    fun getStoreLogoUrl(): String =
        sharedPref.getString(Keys.STORE_LOGO, "") ?: ""

    fun saveStore(store: Store) {
        editor.apply {
            putString(Keys.STORE_ID, store.id)
            putString(Keys.STORE_NAME, store.name)
            putString(Keys.STORE_PHONE, store.phone)
            putString(Keys.STORE_LOCATION, store.location)
            putString(Keys.STORE_OWNER, store.ownerId)
            putString(Keys.STORE_PLAN, store.plan)
            putInt(Keys.PLAN_PRODUCT_LIMIT, store.planProductLimit)
            putInt(Keys.PLAN_OPERATION_LIMIT, store.planOperationLimit)
            putString(Keys.STORE_LOGO, store.logoUrl)
            putString(Keys.RESET_DATE, store.resetDate)
        }.apply()
    }

    fun getStore(): Store {
        return Store(
            id = sharedPref.getString(Keys.STORE_ID, "")!!,
            name = sharedPref.getString(Keys.STORE_NAME, "")!!,
            location = sharedPref.getString(Keys.STORE_LOCATION, "")!!,
            phone = sharedPref.getString(Keys.STORE_PHONE, "")!!,
            ownerId = sharedPref.getString(Keys.STORE_OWNER, "")!!,
            plan = sharedPref.getString(Keys.STORE_PLAN, "")!!,
            planProductLimit = sharedPref.getInt(Keys.PLAN_PRODUCT_LIMIT, 0),
            planOperationLimit = sharedPref.getInt(Keys.PLAN_OPERATION_LIMIT, 0),
            logoUrl = sharedPref.getString(Keys.STORE_LOGO, "")!!,
            resetDate = sharedPref.getString(Keys.RESET_DATE, "")!!,
            productsCount = 0,
            operationsCount = 0
        )
    }

    /////////////////////////////////////////////////
    ////////////// Sync timestamps //////////////////
    /////////////////////////////////////////////////
    private fun setTimestamp(key: String) {
        editor.putString(key, DateHelper.getCurrentTimestampTz()).apply()
    }

    private fun getTimestamp(key: String): String =
        sharedPref.getString(key, "") ?: ""

    internal fun setLastProductsUpdate() = setTimestamp(Keys.LAST_PRODUCTS_UPDATE)
    fun getLastProductsUpdate() = getTimestamp(Keys.LAST_PRODUCTS_UPDATE)

    internal fun setLastBillsUpdate() = setTimestamp(Keys.LAST_BILLS_UPDATE)
    fun getLastBillsUpdate() = getTimestamp(Keys.LAST_BILLS_UPDATE)

    internal fun setLastSalesUpdate() = setTimestamp(Keys.LAST_SALES_UPDATE)
    fun getLastSalesUpdate() = getTimestamp(Keys.LAST_SALES_UPDATE)

    internal fun setLastExpensesUpdate() = setTimestamp(Keys.LAST_EXPENSES_UPDATE)
    fun getLastExpensesUpdate() = getTimestamp(Keys.LAST_EXPENSES_UPDATE)
}
