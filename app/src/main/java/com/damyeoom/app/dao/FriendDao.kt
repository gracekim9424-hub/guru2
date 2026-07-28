package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.damyeoom.app.entity.FriendEntity

// 친구 데이터에 접근하는 DAO
@Dao
interface FriendDao {

    // 친구 추가
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(friend: FriendEntity): Long

    // 사용자별 친구 목록 조회
    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getFriends(userId: Int): List<FriendEntity>

    // 친구 삭제
    @Delete
    suspend fun delete(friend: FriendEntity)
}