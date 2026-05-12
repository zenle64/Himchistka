package com.example.drycleaning.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.drycleaning.data.entity.ServicePrice
import kotlinx.coroutines.flow.Flow

/** DAO для работы с прайс-листом */
@Dao
interface ServicePriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servicePrice: ServicePrice): Long

    @Update
    suspend fun update(servicePrice: ServicePrice)

    @Delete
    suspend fun delete(servicePrice: ServicePrice)

    @Query("SELECT * FROM service_prices ORDER BY itemType ASC, serviceType ASC")
    fun getAllPrices(): Flow<List<ServicePrice>>

    @Query("SELECT * FROM service_prices WHERE itemType = :itemType AND serviceType = :serviceType LIMIT 1")
    suspend fun getPrice(itemType: String, serviceType: String): ServicePrice?

    @Query("SELECT DISTINCT itemType FROM service_prices ORDER BY itemType ASC")
    fun getAllItemTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT serviceType FROM service_prices ORDER BY serviceType ASC")
    fun getAllServiceTypes(): Flow<List<String>>
}
