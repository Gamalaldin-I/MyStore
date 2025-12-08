package com.example.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object NotificationTimeUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"

    // ✅ Parse date safely
    private fun parseDate(dateString: String): Calendar? {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")   // مهم عشان XXX
            val date = sdf.parse(dateString) ?: return null

            Calendar.getInstance().apply { time = date }
        } catch (e: Exception) {
            null
        }
    }

    /** ✅ Relative Time: "Just now", "5m ago", "2h ago", etc */
    fun getRelativeTime(dateString: String): String {
        val notificationTime = parseDate(dateString) ?: return dateString
        val now = Calendar.getInstance()

        val diffMillis = now.timeInMillis - notificationTime.timeInMillis

        return when {
            diffMillis < 60_000 -> "Just now"
            diffMillis < 3_600_000 -> "${diffMillis / 60_000}m ago"
            diffMillis < 86_400_000 -> "${diffMillis / 3_600_000}h ago"
            isYesterday(notificationTime, now) -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Yesterday at ${timeFormat.format(notificationTime.time)}"
            }
            isSameWeek(notificationTime, now) -> {
                val dayFormat = SimpleDateFormat("EEEE 'at' h:mm a", Locale.getDefault())
                dayFormat.format(notificationTime.time)
            }
            isSameYear(notificationTime, now) -> {
                val dateFormat = SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault())
                dateFormat.format(notificationTime.time)
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(notificationTime.time)
            }
        }
    }

    /** ✅ Extract full parts from ISO date */
    data class DateParts(
        val year: Int,
        val month: Int,   // 1 - 12
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int,
        val timeZone: String
    )

    fun extractDateParts(dateString: String): DateParts? {
        val cal = parseDate(dateString) ?: return null

        return DateParts(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
            timeZone = cal.timeZone.id
        )
    }

    fun isToday(dateString: String): Boolean {
        val notiTime = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        return isSameDay(notiTime, now)
    }

    fun isYesterday(dateString: String): Boolean {
        val notiTime = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        return isYesterday(notiTime, now)
    }

    fun isThisWeek(dateString: String): Boolean {
        val notiTime = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        return isSameWeek(notiTime, now)
    }

    // Helpers
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(notificationTime: Calendar, now: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(notificationTime, yesterday)
    }

    private fun isSameWeek(notificationTime: Calendar, now: Calendar): Boolean {
        return notificationTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                notificationTime.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameYear(notificationTime: Calendar, now: Calendar): Boolean {
        return notificationTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }
}
