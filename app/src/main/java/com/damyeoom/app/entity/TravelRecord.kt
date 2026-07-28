package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 여행 기록을 저장하는 테이블
@Entity(tableName = "travel_records")
data class TravelRecord(

    // 여행 기록의 고유 ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 기록을 작성한 사용자 ID
    val userId: Int,

    // 여행 지역
    val region: String,

    // 방문 날짜
    val visitDate: String,

    // 여행 메모
    val memo: String = "",

    // 방문 장소 이름
    val placeName: String = "",

    // 저장한 사진의 URI
    val imageUri: String? = null,

    // 장소의 지도 좌표
    val latitude: Double? = null,
    val longitude: Double? = null,

    // 기록 생성 시각
    val createdAt: Long = System.currentTimeMillis()
)