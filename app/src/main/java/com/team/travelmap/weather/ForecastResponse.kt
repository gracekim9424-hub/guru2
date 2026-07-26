package com.team.travelmap.weather

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,               // 유닉스 타임스탬프
    val main: MainInfo,
    val weather: List<WeatherInfo>,
    val dt_txt: String          // "2026-07-24 12:00:00" 형태
)

data class MainInfo(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)

data class WeatherInfo(
    val main: String,
    val description: String,
    val icon: String
)

data class City(
    val name: String
)