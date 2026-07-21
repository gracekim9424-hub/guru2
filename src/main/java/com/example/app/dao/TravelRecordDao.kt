package com.example.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.app.data.entity.TravelRecord

@Dao
interface TravelRecordDao {

    @Insert
    suspend fun insert(record: TravelRecord): Long

    @Query("SELECT * FROM travel_records ORDER BY createdAt DESC")
    suspend fun getAllRecords(): List<TravelRecord>

    @Query("SELECT * FROM travel_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): TravelRecord?

    @Update
    suspend fun update(record: TravelRecord)

    @Delete
    suspend fun delete(record: TravelRecord)
}