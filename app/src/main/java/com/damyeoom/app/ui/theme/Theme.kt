package com.damyeoom.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DamyeoomColorScheme = lightColorScheme(
    primary = ButtonDark,
    onPrimary = Color.White,
    secondary = AccentTeal,
    background = BgLight,
    onBackground = TextPrimary,
    surface = BgLight,
    onSurface = TextPrimary,
    surfaceVariant = CardGray,
)

@Composable
fun DamyeoomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DamyeoomColorScheme,
        typography = DamyeoomTypography,
        content = content
    )
}
