package com.damyeoom.app.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GeocodeApiService {
    @GET("map-geocode/v2/geocode")
    suspend fun getGeocode(
        @Query("query") query: String,
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String
    ): GeocodeResponse
}