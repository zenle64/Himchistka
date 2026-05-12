package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.InventoryDao
import com.example.drycleaning.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы со складом расходных материалов.
 * Добавление, списание, уведомления о низком остатке.
 */
@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    fun getAllItems(): Flow<List<InventoryItem>> = inventoryDao.getAllItems()

    fun getLowStockItems(): Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()

    suspend fun getItemById(id: Long): InventoryItem? = inventoryDao.getItemById(id)

    suspend fun insertItem(item: InventoryItem): Long = inventoryDao.insert(item)

    suspend fun updateItem(item: InventoryItem) = inventoryDao.update(item)

    suspend fun deleteItem(item: InventoryItem) = inventoryDao.delete(item)

    suspend fun deductQuantity(id: Long, amount: Double) = inventoryDao.deductQuantity(id, amount)

    fun searchItems(query: String): Flow<List<InventoryItem>> = inventoryDao.searchItems(query)

    suspend fun getAllItemsList(): List<InventoryItem> = inventoryDao.getAllItemsList()
}
