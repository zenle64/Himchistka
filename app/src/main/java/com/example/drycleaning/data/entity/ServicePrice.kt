package com.example.drycleaning.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность прайс-листа.
 * Хранит цены на услуги по типу изделия и типу услуги.
 */
@Entity(tableName = "service_prices")
data class ServicePrice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemType: String,    // тип изделия (Пальто, Костюм и т.д.)
    val serviceType: String, // тип услуги (Чистка, Глажка и т.д.)
    val price: Double
)
