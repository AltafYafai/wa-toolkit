package com.wa.toolkit.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object ColorPresets {
    fun getLightColorScheme(preset: String): ColorScheme {
        return when (preset) {
            "blue" -> lightColorScheme(primary = Color(0xFF2196F3))
            "cyan" -> lightColorScheme(primary = Color(0xFF00BCD4))
            "purple" -> lightColorScheme(primary = Color(0xFF9C27B0))
            "orange" -> lightColorScheme(primary = Color(0xFFFF9800))
            "red" -> lightColorScheme(primary = Color(0xFFF44336))
            "pink" -> lightColorScheme(primary = Color(0xFFE91E63))
            else -> lightColorScheme(primary = Color(0xFF1B8755)) // Green default
        }
    }

    fun getDarkColorScheme(preset: String): ColorScheme {
        return when (preset) {
            "blue" -> darkColorScheme(primary = Color(0xFF90CAF9))
            "cyan" -> darkColorScheme(primary = Color(0xFF80DEEA))
            "purple" -> darkColorScheme(primary = Color(0xFFCE93D8))
            "orange" -> darkColorScheme(primary = Color(0xFFFFCC80))
            "red" -> darkColorScheme(primary = Color(0xFFEF9A9A))
            "pink" -> darkColorScheme(primary = Color(0xFFF48FB1))
            else -> darkColorScheme(primary = Color(0xFF81C784)) // Green default
        }
    }
}
