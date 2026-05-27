package com.beautystock.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun formatDateForApi(date: Date): String {
        return dateFormat.format(date)
    }
    
    fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val date = dateFormat.parse(dateString)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun isExpired(expirationDateString: String?): Boolean {
        if (expirationDateString.isNullOrEmpty()) return false
        val expirationDate = parseDate(expirationDateString) ?: return false
        return Date().after(expirationDate)
    }
    
    fun isExpiringWithin(expirationDateString: String?, days: Int = 7): Boolean {
        if (expirationDateString.isNullOrEmpty()) return false
        val expirationDate = parseDate(expirationDateString) ?: return false
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val today = Date()
        return today.before(expirationDate) && calendar.time.after(expirationDate)
    }
}

object PriceFormatter {
    fun formatPrice(price: Double?): String {
        if (price == null) return "N/A"
        return String.format("₱%.2f", price)
    }
}

object StatusFormatter {
    fun getProductStatus(expirationDateString: String?, openedDateString: String?): String {
        if (DateFormatter.isExpired(expirationDateString)) {
            return "Expired"
        }
        if (DateFormatter.isExpiringWithin(expirationDateString, 7)) {
            return "Expiring Soon"
        }
        if (openedDateString != null) {
            val openedDate = DateFormatter.parseDate(openedDateString)
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = openedDate ?: today
            calendar.add(Calendar.DAY_OF_YEAR, 30)
            if (today.after(calendar.time)) {
                return "Running Out"
            }
        }
        return "Available"
    }
    
    fun getStatusColor(status: String): String {
        return when (status) {
            "Expired" -> "#F44336"
            "Expiring Soon" -> "#FFC107"
            "Running Out" -> "#FF9800"
            else -> "#4CAF50"
        }
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
}
