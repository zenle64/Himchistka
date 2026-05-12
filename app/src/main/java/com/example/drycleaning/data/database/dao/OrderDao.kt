package com.example.drycleaning.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import kotlinx.coroutines.flow.Flow

/** DAO для работы с заказами */
@Dao
interface OrderDao {

    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY price DESC")
    fun getAllOrdersByPrice(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY dueDate ASC")
    fun getAllOrdersByDueDate(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY status ASC, createdAt DESC")
    fun getAllOrdersByStatus(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderByIdFlow(id: Long): Flow<Order?>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getOrdersByClient(clientId: Long): Flow<List<Order>>

    @Query("""
        SELECT * FROM orders 
        WHERE CAST(id AS TEXT) LIKE '%' || :query || '%' 
           OR clientName LIKE '%' || :query || '%'
           OR clientPhone LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchOrders(query: String): Flow<List<Order>>

    @Query("SELECT COUNT(*) FROM orders WHERE status != 'DELIVERED'")
    fun getActiveOrderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'")
    fun getCompletedOrderCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(price), 0) FROM orders WHERE status = 'DELIVERED' AND createdAt >= :startOfDay")
    fun getDailyRevenue(startOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(price), 0) FROM orders WHERE status = 'DELIVERED' AND createdAt >= :startOfMonth")
    fun getMonthlyRevenue(startOfMonth: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(price), 0) FROM orders WHERE status = 'DELIVERED' AND createdAt BETWEEN :start AND :end")
    fun getRevenueForPeriod(start: Long, end: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt BETWEEN :start AND :end")
    fun getOrderCountForPeriod(start: Long, end: Long): Flow<Int>

    @Query("SELECT COALESCE(AVG(price), 0) FROM orders WHERE status = 'DELIVERED' AND createdAt BETWEEN :start AND :end")
    fun getAverageCheckForPeriod(start: Long, end: Long): Flow<Double>

    @Query("SELECT serviceType, COUNT(*) as cnt FROM orders GROUP BY serviceType ORDER BY cnt DESC")
    fun getPopularServices(): Flow<List<ServiceStat>>

    @Query("SELECT * FROM orders WHERE status = 'READY'")
    suspend fun getReadyOrders(): List<Order>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' AND status != 'READY' AND dueDate < :now")
    suspend fun getOverdueOrders(now: Long): List<Order>

    @Query("SELECT COALESCE(SUM(price), 0) FROM orders WHERE status = 'DELIVERED' AND clientId = :clientId")
    fun getClientTotalSpent(clientId: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM orders WHERE clientId = :clientId")
    fun getClientOrderCount(clientId: Long): Flow<Int>

    @Query("SELECT * FROM orders WHERE status = 'DELIVERED' AND createdAt BETWEEN :start AND :end ORDER BY createdAt ASC")
    suspend fun getDeliveredOrdersForPeriod(start: Long, end: Long): List<Order>
}

/** Результат запроса популярных услуг */
data class ServiceStat(
    val serviceType: String,
    val cnt: Int
)
