package com.example.mvp.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

object AccessibilityManager {
    data class AccessibilitySettings(
        val fontSizeScale: Float = 1.0f,
        val highContrast: Boolean = false,
        val reduceMotion: Boolean = false
    )
    
    @Composable
    fun getScaledTextStyle(baseStyle: TextStyle, scale: Float): TextStyle {
        return baseStyle.copy(
            fontSize = (baseStyle.fontSize.value * scale).sp
        )
    }
    
    @Composable
    fun getAccessibleColorScheme() = if (AccessibilitySettings().highContrast) {
        // High contrast color scheme
        MaterialTheme.colorScheme.copy(
            primary = androidx.compose.ui.graphics.Color(0xFF000000),
            onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            background = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            onBackground = androidx.compose.ui.graphics.Color(0xFF000000)
        )
    } else {
        MaterialTheme.colorScheme
    }
}

