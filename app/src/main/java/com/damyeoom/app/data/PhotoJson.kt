package com.damyeoom.app.data

import android.net.Uri
import com.google.gson.Gson

fun encodePhotos(uris: List<Uri>): String? {
    if (uris.isEmpty()) return null
    return Gson().toJson(uris.map { it.toString() })
}

fun decodePhotos(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        Gson().fromJson(json, Array<String>::class.java).toList()
    } catch (e: Exception) {
        emptyList()
    }
}