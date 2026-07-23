package com.example.app.data.dao

import androidx.room.*
import com.example.app.entity.ChecklistItem

@Dao
interface ChecklistDao {

    @Insert
    suspend fun insert(item: ChecklistItem): Long

    @Query("SELECT * FROM checklist_items WHERE travelRecordId = :travelId")
    suspend fun getChecklistByTravelId(travelId: Long): List<ChecklistItem>

    @Update
    suspend fun update(item: ChecklistItem)

    @Delete
    suspend fun delete(item: ChecklistItem)
}