package com.team.travelmap.weather

// 현재 날씨 API의 전체 응답
data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val name: String
)

// 현재 날씨 상태 정보
data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

// 현재 온도와 체감온도, 습도 정보
data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)