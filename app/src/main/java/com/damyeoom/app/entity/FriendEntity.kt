package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 친구 정보를 저장하는 테이블
@Entity(
    tableName = "friends",
    indices = [Index(value = ["userId", "name"], unique = true)]
)
data class FriendEntity(

    // 친구 데이터의 고유 ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 친구를 등록한 사용자 ID
    val userId: Int,

    // 친구 이름
    val name: String,

    // 친구 표시 색상
    val colorArgb: Long,

    // 등록 시각
    val createdAt: Long = System.currentTimeMillis()
)