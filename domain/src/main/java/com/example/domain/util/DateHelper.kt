package com.example.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }


    fun getCurrentTime(): String {
        val dateTimeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val date = Date()
        return dateTimeFormat.format(date)
    }
}