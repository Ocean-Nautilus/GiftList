package com.example.wishlist228.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wishlist228.models.ThemeMode

private val LightColors = lightColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0E8),
    onPrimaryContainer = Color(0xFF3F0016),
    secondary = BrandPurple,
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF1E1E24),
    surface = Color.White,
    onSurface = Color(0xFF1E1E24),
    onSurfaceVariant = Color(0xFF6B6B73)
)

private val DarkColors = darkColorScheme(
    primary = BrandPinkLight,
    onPrimary = Color(0xFF5C0020),
    primaryContainer = Color(0xFF7A1033),
    onPrimaryContainer = Color(0xFFFFD9E1),
    secondary = BrandPurpleLight,
    onSecondary = Color(0xFF2B0A8F),
    background = Color(0xFF121214),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFC9C5CA)
)

/** `true` when the dark colors must be used for the chosen [mode]. */
@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Material 3 theme of the app. The user chooses light, dark or "as in the system" in the settings. */
@Composable
fun GiftListTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme(themeMode)) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
