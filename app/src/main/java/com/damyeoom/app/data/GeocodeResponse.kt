package com.damyeoom.app.data

// 지오코딩 API의 전체 응답 데이터
data class GeocodeResponse(
    val status: String,
    val addresses: List<Address>
)

// 주소와 좌표 정보를 저장하는 데이터
data class Address(
    val roadAddress: String,
    val jibunAddress: String,
    val x: String, // 경도
    val y: String  // 위도
)