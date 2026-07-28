package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.damyeoom.app.entity.ChecklistItem

// 여행 체크리스트 데이터에 접근하는 DAO
@Dao
interface ChecklistDao {

    // 체크리스트 항목 추가
    @Insert
    suspend fun insert(item: ChecklistItem): Long

    // 특정 여행 기록의 체크리스트 조회
    @Query(
        "SELECT * FROM checklist_items " +
                "WHERE travelRecordId = :travelId"
    )
    suspend fun getChecklistByTravelId(
        travelId: Long
    ): List<ChecklistItem>

    // 체크리스트 항목 수정
    @Update
    suspend fun update(item: ChecklistItem)

    // 체크리스트 항목 삭제
    @Delete
    suspend fun delete(item: ChecklistItem)
}