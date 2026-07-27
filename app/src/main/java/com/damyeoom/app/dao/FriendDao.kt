package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.damyeoom.app.entity.FriendEntity

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(friend: FriendEntity): Long

    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getFriends(userId: Int): List<FriendEntity>

    @Delete
    suspend fun delete(friend: FriendEntity)
}
