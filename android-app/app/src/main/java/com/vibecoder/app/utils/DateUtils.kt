package com.vibecoder.app.utils

import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    fun getCurrentDate(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    
    fun formatDate(date: LocalDate): String {
        return date.toString() // This gives YYYY-MM-DD format
    }
    
    fun formatDateForDisplay(date: LocalDate): String {
        val javaDate = Date(date.toEpochDays() * 24 * 60 * 60 * 1000L)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(javaDate)
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun isToday(date: LocalDate): Boolean {
        return date == getCurrentDate()
    }
    
    fun isPast(date: LocalDate): Boolean {
        return date < getCurrentDate()
    }
    
    fun isFuture(date: LocalDate): Boolean {
        return date > getCurrentDate()
    }
    
    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Int {
        return endDate.toEpochDays() - startDate.toEpochDays()
    }
    
    fun daysFromToday(date: LocalDate): Int {
        return daysBetween(getCurrentDate(), date)
    }
    
    fun getRelativeDateString(date: LocalDate): String {
        val daysFromToday = daysFromToday(date)
        return when {
            daysFromToday == 0 -> "Today"
            daysFromToday == 1 -> "Tomorrow"
            daysFromToday == -1 -> "Yesterday"
            daysFromToday > 1 -> "In $daysFromToday days"
            daysFromToday < -1 -> "${-daysFromToday} days ago"
            else -> formatDateForDisplay(date)
        }
    }
    
    // Extension function to convert LocalDate to epoch days for Java Date conversion
    private fun LocalDate.toEpochDays(): Long {
        return this.toEpochDays().toLong()
    }
}