package com.example.drycleaning.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность пользователя системы (менеджер / администратор).
 * Хранит учётные данные для авторизации.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: UserRole = UserRole.MANAGER
)

/** Роли пользователей */
enum class UserRole {
    MANAGER,
    ADMIN
}
