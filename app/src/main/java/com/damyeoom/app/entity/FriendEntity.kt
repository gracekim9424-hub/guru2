package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friends",
    indices = [Index(value = ["userId", "name"], unique = true)]
)
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int,
    val name: String,
    val colorArgb: Long,
    val createdAt: Long = System.currentTimeMillis()
)
