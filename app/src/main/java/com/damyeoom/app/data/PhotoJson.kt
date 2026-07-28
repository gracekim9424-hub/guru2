package com.damyeoom.app.data

import android.net.Uri
import com.google.gson.Gson

// 사진 URI 목록을 JSON 문자열로 변환
fun encodePhotos(uris: List<Uri>): String? {
    if (uris.isEmpty()) return null

    return Gson().toJson(
        uris.map { uri -> uri.toString() }
    )
}

// JSON 문자열을 사진 URI 문자열 목록으로 복원
fun decodePhotos(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()

    return try {
        Gson()
            .fromJson(json, Array<String>::class.java)
            .toList()
    } catch (e: Exception) {
        emptyList()
    }
}