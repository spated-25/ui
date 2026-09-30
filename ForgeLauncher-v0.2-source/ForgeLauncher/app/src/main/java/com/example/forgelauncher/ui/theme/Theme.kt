package com.example.forgelauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.forgelauncher.data.preferences.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC7BFFF),
    onPrimary = Color(0xFF2B2067),
    primaryContainer = Color(0xFF443593),
    onPrimaryContainer = Color(0xFFE6E0FF),
    secondary = Color(0xFFB7C9FF),
    background = Color(0xFF101014),
    surface = Color(0xFF17171D),
    surfaceVariant = Color(0xFF2A2932),
    onSurface = Color(0xFFF1EFF7),
    onSurfaceVariant = Color(0xFFC9C5D2),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5C46B8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DEFF),
    onPrimaryContainer = Color(0xFF201057),
    secondary = Color(0xFF4F6097),
    background = Color(0xFFF9F7FF),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFE8E4EE),
    onSurface = Color(0xFF1C1B20),
    onSurfaceVariant = Color(0xFF49454F),
)

@Composable
fun ForgeLauncherTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
