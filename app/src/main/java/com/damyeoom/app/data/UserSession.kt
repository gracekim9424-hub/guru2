package com.damyeoom.app.data

import androidx.compose.runtime.mutableStateOf

object UserSession {
    var currentUserId = mutableStateOf<Int?>(null)
        private set

    fun login(userId: Int) {
        currentUserId.value = userId
    }

    fun logout() {
        currentUserId.value = null
    }
}