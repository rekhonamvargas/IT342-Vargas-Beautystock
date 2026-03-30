package com.beautystock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light scheme ─────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Pink40,
    onPrimary          = Color.White,
    primaryContainer   = Pink90,
    onPrimaryContainer = Pink10,

    secondary            = PinkGrey50,
    onSecondary          = Color.White,
    secondaryContainer   = PinkGrey90,
    onSecondaryContainer = PinkGrey30,

    tertiary            = Terracotta40,
    onTertiary          = Color.White,
    tertiaryContainer   = Terracotta90,
    onTertiaryContainer = Terracotta30,

    error            = Error40,
    onError          = Color.White,
    errorContainer   = Error90,
    onErrorContainer = Error10,

    background   = Neutral99,
    onBackground = Neutral10,

    surface          = Neutral99,
    onSurface        = Neutral10,
    surfaceVariant   = NeutralVar90,
    onSurfaceVariant = NeutralVar30,
    outline          = NeutralVar50,
    outlineVariant   = NeutralVar80,
)

// ── Dark scheme ──────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Pink80,
    onPrimary          = Pink20,
    primaryContainer   = Pink40,
    onPrimaryContainer = Pink90,

    secondary            = PinkGrey80,
    onSecondary          = PinkGrey30,
    secondaryContainer   = PinkGrey50,
    onSecondaryContainer = PinkGrey90,

    tertiary            = Terracotta80,
    onTertiary          = Terracotta30,
    tertiaryContainer   = Terracotta40,
    onTertiaryContainer = Terracotta90,

    error            = Error90,
    onError          = Error10,
    errorContainer   = Error40,
    onErrorContainer = Error90,

    background   = Neutral10,
    onBackground = Neutral90,

    surface          = Neutral10,
    onSurface        = Neutral90,
    surfaceVariant   = NeutralVar30,
    onSurfaceVariant = NeutralVar80,
    outline          = NeutralVar60,
    outlineVariant   = NeutralVar30,
)

// ── Theme ────────────────────────────────────────────────────────────
@Composable
fun BeautyStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Tint status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BeautyStockTypography,
        content = content
    )
}
