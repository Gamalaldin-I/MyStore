package com.example.data.local.sharedPrefs

import android.content.Context

class SharedPref(context: Context){
    private val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    fun saveCash(cash: Double) {
        sharedPref.edit().putFloat("cash", cash.toFloat()).apply()
    }
    fun getCash(): Double {
        return sharedPref.getFloat("cash", 0f).toDouble()
    }

}