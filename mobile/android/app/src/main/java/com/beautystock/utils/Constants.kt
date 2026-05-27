package com.beautystock.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors
object BeautyColors {
    val Primary = Color(0xFFEC4899)
    val PrimaryDark = Color(0xFFBE185D)
    val PrimaryLight = Color(0xFFFCE7F3)
    
    val Secondary = Color(0xFFD4A017)
    val SecondaryDark = Color(0xFF9A6E10)
    val SecondaryLight = Color(0xFFFFF4D6)
    
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF3B82F6)
    
    val Background = Color(0xFFF3EEE9)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1A1008)
    val TextSecondary = Color(0xFF7D675C)
    val TextHint = Color(0xFFBCAAA2)
    val Divider = Color(0xFFE8DDD6)
    
    val Available = Color(0xFF4CAF50)
    val ExpiringSoon = Color(0xFFFFC107)
    val RunningOut = Color(0xFFFF9800)
    val Expired = Color(0xFFF44336)
}

// Spacing
object BeautySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val xxxxl = 40.dp
}

// Font Sizes
object BeautyFontSizes {
    val small = 12.sp
    val body = 14.sp
    val bodyLarge = 16.sp
    val title = 18.sp
    val headline = 20.sp
    val display = 32.sp
}

// Border Radius
object BeautyRadius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
}

// Constants
object AppConstants {
    const val MIN_PASSWORD_LENGTH = 8
    const val PRODUCT_CATEGORIES = "Skincare,Makeup,Fragrance,Haircare,Bodycare,Other"
    const val AGE_RANGES = "Youth,Adult"
    
    // Placeholder cities
    val POPULAR_CITIES = listOf(
        "Manila",
        "Cebu",
        "Davao",
        "Makati",
        "Pasig",
        "Taguig",
        "Quezon City",
        "Iloilo",
        "Bacolod",
        "Cabanatuan",
        "Zamboanga",
        "Baguio",
        "Tagaytay",
        "Boracay",
        "Palawan",
        "Cagayan",
        "Nueva Ecija",
        "Bulacan",
        "Cavite",
        "Laguna"
    )
}
