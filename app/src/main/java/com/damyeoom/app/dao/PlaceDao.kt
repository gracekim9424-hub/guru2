package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.damyeoom.app.entity.PlaceEntity
//import com.example.app.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE region = :region")
    fun getPlacesByRegion(region: String): Flow<List<PlaceEntity>>

    @Query(
        "SELECT * FROM places " +
                "WHERE region = :region AND category = :category"
    )
    fun getPlacesByRegionAndCategory(
        region: String,
        category: String
    ): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    suspend fun getPlaceById(placeId: Int): PlaceEntity?

    @Query("SELECT COUNT(*) FROM places")
    suspend fun getPlaceCount(): Int
}