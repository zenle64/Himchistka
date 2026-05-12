package com.example.drycleaning.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.drycleaning.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

/** DAO для работы со складом расходных материалов */
@Dao
interface InventoryDao {

    @Insert
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory WHERE quantity <= minQuantity")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("UPDATE inventory SET quantity = quantity - :amount, updatedAt = :now WHERE id = :id AND quantity >= :amount")
    suspend fun deductQuantity(id: Long, amount: Double, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM inventory WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory ORDER BY name ASC")
    suspend fun getAllItemsList(): List<InventoryItem>
}
