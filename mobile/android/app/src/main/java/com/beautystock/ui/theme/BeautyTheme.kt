package com.beautystock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.beautystock.utils.BeautyColors

private val BeautyLightColorScheme = lightColorScheme(
    primary = BeautyColors.Primary,
    onPrimary = BeautyColors.Surface,
    primaryContainer = BeautyColors.PrimaryLight,
    onPrimaryContainer = BeautyColors.PrimaryDark,
    secondary = BeautyColors.Secondary,
    onSecondary = BeautyColors.Surface,
    secondaryContainer = BeautyColors.SecondaryLight,
    onSecondaryContainer = BeautyColors.SecondaryDark,
    tertiary = BeautyColors.Info,
    background = BeautyColors.Background,
    onBackground = BeautyColors.TextPrimary,
    surface = BeautyColors.Surface,
    onSurface = BeautyColors.TextPrimary,
    surfaceVariant = Color(0xFFFBF6F2),
    onSurfaceVariant = BeautyColors.TextSecondary,
    outline = BeautyColors.Divider,
    error = BeautyColors.Error,
    onError = BeautyColors.Surface
)

private val BeautyTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun BeautyStockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BeautyLightColorScheme,
        typography = BeautyTypography,
        content = content
    )
}