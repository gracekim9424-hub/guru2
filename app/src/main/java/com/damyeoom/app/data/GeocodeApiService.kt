package com.damyeoom.app.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 네이버 지오코딩 API 요청 인터페이스
interface GeocodeApiService {

    // 주소를 위도와 경도로 변환
    @GET("map-geocode/v2/geocode")
    suspend fun getGeocode(
        @Query("query") query: String,
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String
    ): GeocodeResponse
}