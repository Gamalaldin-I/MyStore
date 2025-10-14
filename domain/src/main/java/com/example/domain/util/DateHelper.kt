package com.example.domain.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateHelper {
     const val DAY ="Day"
     const val WEEK="Week"
     const val MONTH="Month"
     const val YEAR="Year"




    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }


    fun getCurrentTime(): String {
        val dateTimeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val date = Date()
        return dateTimeFormat.format(date)
    }



    fun giveStartAndEndDateFromToDay(duration: String): Pair<String, String> {
        val today = LocalDate.now()
            val (startDate, endDate) = when(duration) {
                DAY -> today to today
                WEEK -> today.minusDays(7) to today
                MONTH -> today.withDayOfMonth(1) to today // أول يوم في الشهر
                YEAR -> today.withDayOfYear(1) to today  // أول يوم في السنة
                else -> today.minusWeeks(1) to today
            }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = startDate.format(formatter)
        val end = endDate.format(formatter)
        return Pair(start, end)
    }
}