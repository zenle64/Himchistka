package com.example.drycleaning.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность клиента химчистки.
 * Содержит контактные данные и статистику.
 */
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
