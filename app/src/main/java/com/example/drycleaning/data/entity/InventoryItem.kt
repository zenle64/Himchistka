package com.example.drycleaning.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность расходного материала на складе.
 * Отслеживает количество и минимальный порог для уведомлений.
 */
@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String,         // единица измерения (л, кг, шт)
    val quantity: Double,
    val minQuantity: Double,  // минимальный остаток для уведомления
    val pricePerUnit: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
