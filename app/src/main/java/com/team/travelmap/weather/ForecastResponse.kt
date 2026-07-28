package com.team.travelmap.weather

// 날씨 예보 API의 전체 응답
data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

// 시간대별 날씨 예보 정보
data class ForecastItem(
    // 예보 시간의 유닉스 타임스탬프
    val dt: Long,

    // 온도와 습도 정보
    val main: MainInfo,

    // 날씨 상태 정보
    val weather: List<WeatherInfo>,

    // 예보 날짜와 시간
    val dt_txt: String
)

// 예보 온도 및 습도 정보
data class MainInfo(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)

// 예보 날씨 상태 정보
data class WeatherInfo(
    val main: String,
    val description: String,
    val icon: String
)

// 예보 지역 정보
data class City(
    val name: String
)