package com.team.travelmap.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// OpenWeather API 통신 객체
object RetrofitClient {

    // OpenWeather 기본 주소
    private const val BASE_URL =
        "https://api.openweathermap.org/"

    // 날씨 API 서비스 생성
    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(
                WeatherApiService::class.java
            )
    }
}