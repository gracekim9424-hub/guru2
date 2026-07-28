package com.damyeoom.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.damyeoom.app.entity.User

// 사용자 데이터에 접근하는 DAO
@Dao
interface UserDao {

    // 새로운 사용자 등록
    @Insert
    suspend fun insert(user: User)

    // 이메일과 비밀번호로 로그인 확인
    @Query(
        "SELECT * FROM users " +
                "WHERE email = :email AND password = :password LIMIT 1"
    )
    suspend fun login(
        email: String,
        password: String
    ): User?

    // 이메일로 사용자 조회
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}