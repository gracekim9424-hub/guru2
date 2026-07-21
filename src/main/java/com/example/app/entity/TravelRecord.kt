package com.example.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_records")
data class TravelRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val placeName: String,
    val region: String,
    val visitDate: String,
    val memo: String = "",
    val rating: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)