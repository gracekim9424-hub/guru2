package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_records")
data class TravelRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 이 여행 기록을 작성한 사용자
    val userId: Int,
    // 서울, 경기, 부산처럼 시·도 단위
    val region: String,

    // 예: 2026-07-22
    val visitDate: String,

    // 여행 메모
    val memo: String = "",

    // 사진 한 장의 URI
    val imageUri: String? = null,

    // 지도 위치
    val latitude: Double? = null,
    val longitude: Double? = null,

    // 기록 생성 시각
    val createdAt: Long = System.currentTimeMillis()
)