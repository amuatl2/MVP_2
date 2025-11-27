package com.example.mvp.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ThemeManager {
    val LightColorScheme = lightColorScheme(
        primary = Color(0xFF3A86FF),
        secondary = Color(0xFF6C757D),
        tertiary = Color(0xFF28A745),
        error = Color(0xFFDC3545),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF8F9FA),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFFFFFFFF),
        onTertiary = Color(0xFFFFFFFF),
        onError = Color(0xFFFFFFFF),
        onBackground = Color(0xFF2D3748),
        onSurface = Color(0xFF2D3748),
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = Color(0xFF2D3748)
    )
    
    val DarkColorScheme = darkColorScheme(
        primary = Color(0xFF5A9FFF),
        secondary = Color(0xFF8B9DC3),
        tertiary = Color(0xFF4CAF50),
        error = Color(0xFFEF5350),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFFFFFFFF),
        onTertiary = Color(0xFF000000),
        onError = Color(0xFF000000),
        onBackground = Color(0xFFE0E0E0),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2D2D2D),
        onSurfaceVariant = Color(0xFFE0E0E0)
    )
}

@Composable
fun getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) ThemeManager.DarkColorScheme else ThemeManager.LightColorScheme
}

