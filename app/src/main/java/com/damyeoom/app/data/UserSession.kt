package com.damyeoom.app.data

import androidx.compose.runtime.mutableStateOf

// 현재 로그인한 사용자의 세션 정보 관리
object UserSession {

    // 로그인한 사용자 ID
    var currentUserId = mutableStateOf<Int?>(null)
        private set

    // 로그인 시 사용자 ID 저장
    fun login(userId: Int) {
        currentUserId.value = userId
    }

    // 로그아웃 시 사용자 정보 초기화
    fun logout() {
        currentUserId.value = null
    }
}