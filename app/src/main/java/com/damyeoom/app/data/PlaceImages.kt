package com.damyeoom.app.data

import android.content.Context

// 장소 이미지 URL 또는 로컬 drawable 리소스를 반환
fun resolvePlaceImage(
    context: Context,
    imageUrl: String?
): Any? {

    // 이미지 정보가 없으면 null 반환
    if (imageUrl.isNullOrBlank()) return null

    // 인터넷 이미지 주소이면 그대로 반환
    if (imageUrl.startsWith("http")) {
        return imageUrl
    }

    // 로컬 drawable 이름을 리소스 ID로 변환
    val resId = context.resources.getIdentifier(
        imageUrl,
        "drawable",
        context.packageName
    )

    return if (resId != 0) resId else null
}