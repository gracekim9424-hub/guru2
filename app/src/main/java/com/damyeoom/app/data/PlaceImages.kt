package com.damyeoom.app.data

import android.content.Context

// PlaceEntity.imageUrl 이 실제 URL("http...")이면 그대로 반환,
// 로컬 drawable 파일명("place_xxx")이면 리소스 ID를 찾아서 반환합니다.
// 둘 중 뭘 쓸지 몰라도 이 함수 하나로 다 처리됩니다.
fun resolvePlaceImage(context: Context, imageUrl: String?): Any? {
    if (imageUrl.isNullOrBlank()) return null
    if (imageUrl.startsWith("http")) return imageUrl

    val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
    return if (resId != 0) resId else null
}