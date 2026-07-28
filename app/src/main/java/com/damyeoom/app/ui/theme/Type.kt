package com.damyeoom.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 앱 전체에서 사용하는 글꼴 스타일
val DamyeoomTypography = Typography(

    // 가장 큰 제목
    headlineLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp
    ),

    // 중간 크기 제목
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    // 큰 제목
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    // 중간 제목
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),

    // 큰 본문
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    // 일반 본문
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // 버튼과 라벨 글자
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),

    // 작은 라벨 글자
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)