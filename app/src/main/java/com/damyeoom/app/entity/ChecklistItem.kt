package com.example.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItem(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 어떤 여행 기록의 체크리스트인지
    val travelRecordId: Long,

    // 준비물 이름
    val itemName: String,

    // 체크 여부
    val isChecked: Boolean = false
)