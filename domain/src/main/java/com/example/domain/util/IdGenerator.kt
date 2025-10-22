package com.example.domain.util

import kotlin.random.Random

object IdGenerator {

     private fun generateRandomId(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random()}
            .joinToString("")
    }

    fun generateTimestampedId(plusTime:Int = 6): String {
        val timestamp = System.currentTimeMillis().toString(36)
        val randomPart = generateRandomId(plusTime)
        return "$timestamp-$randomPart"
    }

    fun generateProductId(length: Int = 13): String {
         // أول 12 رقم بيتولّدوا عشوائي
            val baseDigits = (1..12).map { Random.Default.nextInt(0, 10) }

            // حساب الـ checksum (الرقم رقم 13)
            val sum = baseDigits.mapIndexed { index, digit ->
                if ((index + 1) % 2 == 0) digit * 3 else digit
            }.sum()

            val checksum = (10 - (sum % 10)) % 10

            // نرجع الكود النهائي
            return baseDigits.joinToString("") + checksum.toString()
    }

}