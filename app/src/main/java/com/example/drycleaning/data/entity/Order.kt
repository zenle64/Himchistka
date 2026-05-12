package com.example.drycleaning.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность заказа химчистки.
 * clientId ссылается на клиента (0 = клиент не привязан).
 */
@Entity(
    tableName = "orders",
    indices = [Index("clientId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val itemType: String,
    val serviceType: String,
    val receivedDate: Long,
    val dueDate: Long,
    val price: Double,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Статусы заказа */
enum class OrderStatus {
    RECEIVED,    // Принят
    IN_PROGRESS, // В работе
    READY,       // Готов
    DELIVERED    // Выдан
}
