package com.example.domain.useCase.localize

import android.annotation.SuppressLint
import java.util.Locale

object NAE {

    private fun switchDigitsToArabic(input: String): String {
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩,"
        val englishDigits = "0123456789."
        var output = input
        for (i in englishDigits.indices) {
            output = output.replace(englishDigits[i], arabicDigits[i])
        }
        return output
    }


    @SuppressLint("ConstantLocale")
    val local: String = Locale.getDefault().language
    fun Int.ae(): String {
        return if (local == "ar") switchDigitsToArabic(this.toString()) else this.toString()
    }
    fun Double.ae(): String{
        return if (local == "ar") switchDigitsToArabic(this.toString()) else this.toString()
    }
    fun Float.ae(): String{
        return if (local == "ar") switchDigitsToArabic(this.toString()) else this.toString()
    }
}