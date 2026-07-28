package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 장소 정보를 저장하는 테이블
@Entity(tableName = "places")
data class PlaceEntity(

    // 장소의 고유 ID
    @PrimaryKey
    val placeId: Int,

    // 지역과 카테고리 정보
    val region: String,
    val category: String,

    // 장소 기본 정보
    val name: String,
    val address: String,
    val description: String,

    // 장소 좌표
    val latitude: Double,
    val longitude: Double,

    // 장소 이미지 정보
    val imageUrl: String? = null,

    // 장소 추가 정보
    val openingHours: String? = null,
    val phoneNumber: String? = null,
    val recommendedFor: String? = null
)