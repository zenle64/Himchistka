package com.example.drycleaning.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.drycleaning.data.database.dao.ClientDao
import com.example.drycleaning.data.database.dao.InventoryDao
import com.example.drycleaning.data.database.dao.OrderDao
import com.example.drycleaning.data.database.dao.ServicePriceDao
import com.example.drycleaning.data.database.dao.UserDao
import com.example.drycleaning.data.entity.Client
import com.example.drycleaning.data.entity.InventoryItem
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.ServicePrice
import com.example.drycleaning.data.entity.User

/**
 * Основная база данных приложения.
 * Содержит все таблицы: пользователи, клиенты, заказы, склад, прайс-лист.
 */
@Database(
    entities = [
        User::class,
        Client::class,
        Order::class,
        InventoryItem::class,
        ServicePrice::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun orderDao(): OrderDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun servicePriceDao(): ServicePriceDao
}
