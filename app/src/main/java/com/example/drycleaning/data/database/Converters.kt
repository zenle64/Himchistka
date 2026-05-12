package com.example.drycleaning.data.database

import androidx.room.TypeConverter
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.data.entity.UserRole

/** Конвертеры типов для Room */
class Converters {

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)
}
