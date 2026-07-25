package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey
    val placeId: Int,

    val region: String,
    val category: String,
    val name: String,
    val address: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val imageUrl: String? = null
)