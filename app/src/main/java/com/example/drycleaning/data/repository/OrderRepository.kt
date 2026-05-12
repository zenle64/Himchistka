package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.OrderDao
import com.example.drycleaning.data.database.dao.ServiceStat
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Репозиторий для работы с заказами */
@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao
) {
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getAllOrdersByPrice(): Flow<List<Order>> = orderDao.getAllOrdersByPrice()

    fun getAllOrdersByDueDate(): Flow<List<Order>> = orderDao.getAllOrdersByDueDate()

    fun getAllOrdersByStatus(): Flow<List<Order>> = orderDao.getAllOrdersByStatus()

    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> = orderDao.getOrdersByStatus(status)

    fun getOrdersByClient(clientId: Long): Flow<List<Order>> = orderDao.getOrdersByClient(clientId)

    fun searchOrders(query: String): Flow<List<Order>> = orderDao.searchOrders(query)

    fun getOrderByIdFlow(id: Long): Flow<Order?> = orderDao.getOrderByIdFlow(id)

    fun getActiveOrderCount(): Flow<Int> = orderDao.getActiveOrderCount()

    fun getCompletedOrderCount(): Flow<Int> = orderDao.getCompletedOrderCount()

    fun getDailyRevenue(startOfDay: Long): Flow<Double> = orderDao.getDailyRevenue(startOfDay)

    fun getMonthlyRevenue(startOfMonth: Long): Flow<Double> = orderDao.getMonthlyRevenue(startOfMonth)

    fun getRevenueForPeriod(start: Long, end: Long): Flow<Double> = orderDao.getRevenueForPeriod(start, end)

    fun getOrderCountForPeriod(start: Long, end: Long): Flow<Int> = orderDao.getOrderCountForPeriod(start, end)

    fun getAverageCheckForPeriod(start: Long, end: Long): Flow<Double> = orderDao.getAverageCheckForPeriod(start, end)

    fun getPopularServices(): Flow<List<ServiceStat>> = orderDao.getPopularServices()

    fun getClientTotalSpent(clientId: Long): Flow<Double> = orderDao.getClientTotalSpent(clientId)

    fun getClientOrderCount(clientId: Long): Flow<Int> = orderDao.getClientOrderCount(clientId)

    suspend fun getOrderById(id: Long): Order? = orderDao.getOrderById(id)

    suspend fun insertOrder(order: Order): Long = orderDao.insert(order)

    suspend fun updateOrder(order: Order) = orderDao.update(order)

    suspend fun deleteOrder(order: Order) = orderDao.delete(order)

    suspend fun getReadyOrders(): List<Order> = orderDao.getReadyOrders()

    suspend fun getOverdueOrders(now: Long): List<Order> = orderDao.getOverdueOrders(now)

    suspend fun getDeliveredOrdersForPeriod(start: Long, end: Long): List<Order> =
        orderDao.getDeliveredOrdersForPeriod(start, end)
}
