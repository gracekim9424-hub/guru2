package com.damyeoom.app.data

import android.content.Context
import com.damyeoom.app.entity.PlaceEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadPlacesFromAssets(context: Context): List<PlaceEntity> {
    val jsonString = context.assets
        .open("places.json")
        .bufferedReader()
        .use { reader -> reader.readText() }

    val listType = object : TypeToken<List<PlaceEntity>>() {}.type

    return Gson().fromJson(jsonString, listType)
}