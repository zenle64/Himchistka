package com.example.drycleaning.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.drycleaning.data.entity.User

/** DAO для работы с пользователями системы */
@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
