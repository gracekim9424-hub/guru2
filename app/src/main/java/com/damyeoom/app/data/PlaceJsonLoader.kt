package com.damyeoom.app.data

import android.content.Context
import com.damyeoom.app.entity.PlaceEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// assets의 places.json을 장소 목록으로 변환
fun loadPlacesFromAssets(
    context: Context
): List<PlaceEntity> {

    // places.json 파일의 전체 내용 읽기
    val jsonString = context.assets
        .open("places.json")
        .bufferedReader()
        .use { reader ->
            reader.readText()
        }

    // JSON을 변환할 리스트 자료형 지정
    val listType =
        object : TypeToken<List<PlaceEntity>>() {}.type

    // JSON 문자열을 PlaceEntity 목록으로 변환
    return Gson().fromJson(
        jsonString,
        listType
    )
}