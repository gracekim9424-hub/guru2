package com.damyeoom.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 앱에서 사용할 Material 색상 구성
private val DamyeoomColorScheme =
    lightColorScheme(
        primary = ButtonDark,
        onPrimary = Color.White,
        secondary = AccentTeal,
        background = BgLight,
        onBackground = TextPrimary,
        surface = BgLight,
        onSurface = TextPrimary,
        surfaceVariant = CardGray
    )

// 앱 전체에 색상과 글꼴 테마 적용
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