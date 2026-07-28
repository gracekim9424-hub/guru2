package com.team.travelmap.weather

import retrofit2.http.GET
import retrofit2.http.Query

// OpenWeather API 요청 인터페이스
interface WeatherApiService {

    // 현재 날씨 조회
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat")
        lat: Double,

        @Query("lon")
        lon: Double,

        @Query("appid")
        apiKey: String,

        @Query("units")
        units: String = "metric",

        @Query("lang")
        lang: String = "kr"
    ): WeatherResponse

    // 시간대별 날씨 예보 조회
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat")
        lat: Double,

        @Query("lon")
        lon: Double,

        @Query("appid")
        apiKey: String,

        @Query("units")
        units: String = "metric"
    ): ForecastResponse
}