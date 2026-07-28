package com.damyeoom.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 여행 준비물 체크리스트 테이블
@Entity(tableName = "checklist_items")
data class ChecklistItem(

    // 체크리스트 항목의 고유 ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 연결된 여행 기록 ID
    val travelRecordId: Long,

    // 준비물 이름
    val itemName: String,

    // 체크 완료 여부
    val isChecked: Boolean = false
)