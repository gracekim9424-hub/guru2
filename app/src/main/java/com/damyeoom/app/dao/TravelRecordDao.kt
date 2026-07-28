package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.damyeoom.app.entity.TravelRecord

// 여행 기록 데이터에 접근하는 DAO
@Dao
interface TravelRecordDao {

    // 여행 기록 추가
    @Insert
    suspend fun insert(record: TravelRecord): Long

    // 전체 여행 기록을 최신순으로 조회
    @Query("SELECT * FROM travel_records ORDER BY createdAt DESC")
    suspend fun getAllRecords(): List<TravelRecord>

    // 사용자와 지역에 해당하는 여행 기록 조회
    @Query(
        """
        SELECT * FROM travel_records
        WHERE userId = :userId
        AND region = :region
        ORDER BY createdAt DESC
        """
    )
    suspend fun getRecordsByRegion(
        userId: Int,
        region: String
    ): List<TravelRecord>

    // 사용자의 전체 여행 기록 조회
    @Query(
        """
        SELECT * FROM travel_records
        WHERE userId = :userId
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllRecords(userId: Int): List<TravelRecord>

    // 여행 기록 수정
    @Update
    suspend fun update(record: TravelRecord)

    // 여행 기록 삭제
    @Delete
    suspend fun delete(record: TravelRecord)
}