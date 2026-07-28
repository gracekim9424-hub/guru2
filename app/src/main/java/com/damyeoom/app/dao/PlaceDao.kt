package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.damyeoom.app.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

// 장소 데이터에 접근하는 DAO
@Dao
interface PlaceDao {

    // 여러 장소 정보를 한 번에 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    // 전체 장소 목록 조회
    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    // 지역별 장소 목록 조회
    @Query("SELECT * FROM places WHERE region = :region")
    fun getPlacesByRegion(
        region: String
    ): Flow<List<PlaceEntity>>

    // 지역과 카테고리별 장소 목록 조회
    @Query(
        "SELECT * FROM places " +
                "WHERE region = :region AND category = :category"
    )
    fun getPlacesByRegionAndCategory(
        region: String,
        category: String
    ): Flow<List<PlaceEntity>>

    // 장소 ID로 장소 한 개 조회
    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    suspend fun getPlaceById(
        placeId: Int
    ): PlaceEntity?

    // 저장된 장소 개수 조회
    @Query("SELECT COUNT(*) FROM places")
    suspend fun getPlaceCount(): Int
}