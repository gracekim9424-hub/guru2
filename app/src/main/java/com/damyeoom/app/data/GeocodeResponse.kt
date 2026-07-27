package com.damyeoom.app.data

data class GeocodeResponse(
    val status: String,
    val addresses: List<Address>
)

data class Address(
    val roadAddress: String,
    val jibunAddress: String,
    val x: String,  // 경도(longitude)
    val y: String   // 위도(latitude)
)