package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 사용자 정보를 저장하는 테이블
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class User(

    // 사용자의 고유 ID
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,

    // 로그인 이메일
    val email: String,

    // 로그인 비밀번호
    val password: String
)