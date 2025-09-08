package com.example.htopstore.data.local

import android.content.Context

class SharedPref(context: Context){
    private val myPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE)
    private val editor = myPref.edit()

    fun saveCash(cash: Double) {
        editor.putFloat("cash", cash.toFloat())
        editor.commit()
    }
    fun getCash(): Double {
        return myPref.getFloat("cash", 0.0f).toDouble()
    }

}