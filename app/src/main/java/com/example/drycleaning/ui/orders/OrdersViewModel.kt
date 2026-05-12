package com.example.drycleaning.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.data.repository.InventoryRepository
import com.example.drycleaning.data.repository.OrderRepository
import com.example.drycleaning.data.repository.ServicePriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption { DATE, PRICE, STATUS, DUE_DATE }

/** ViewModel для управления заказами */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val servicePriceRepository: ServicePriceRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow<OrderStatus?>(null)
    val statusFilter: StateFlow<OrderStatus?> = _statusFilter

    private val _sortOption = MutableStateFlow(SortOption.DATE)
    val sortOption: StateFlow<SortOption> = _sortOption

    private val _calculatedPrice = MutableStateFlow(0.0)
    val calculatedPrice: StateFlow<Double> = _calculatedPrice

    private val _saveResult = MutableStateFlow<Result<Long>?>(null)
    val saveResult: StateFlow<Result<Long>?> = _saveResult

    @OptIn(ExperimentalCoroutinesApi::class)
    val orders = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            _statusFilter.flatMapLatest { status ->
                if (status != null) {
                    orderRepository.getOrdersByStatus(status)
                } else {
                    _sortOption.flatMapLatest { sort ->
                        when (sort) {
                            SortOption.DATE -> orderRepository.getAllOrders()
                            SortOption.PRICE -> orderRepository.getAllOrdersByPrice()
                            SortOption.STATUS -> orderRepository.getAllOrdersByStatus()
                            SortOption.DUE_DATE -> orderRepository.getAllOrdersByDueDate()
                        }
                    }
                }
            }
        } else {
            orderRepository.searchOrders(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: OrderStatus?) {
        _statusFilter.value = status
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun calculatePrice(itemType: String, serviceType: String) {
        viewModelScope.launch {
            _calculatedPrice.value = servicePriceRepository.calculatePrice(itemType, serviceType)
        }
    }

    fun saveOrder(order: Order) {
        viewModelScope.launch {
            try {
                val id = orderRepository.insertOrder(order)
                autoDeductInventory(order.serviceType)
                _saveResult.value = Result.success(id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrder(order)
                _saveResult.value = Result.success(order.id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            orderRepository.deleteOrder(order)
        }
    }

    fun getOrderById(id: Long) = orderRepository.getOrderByIdFlow(id)

    fun resetSaveResult() {
        _saveResult.value = null
    }

    private suspend fun autoDeductInventory(serviceType: String) {
        try {
            val items = inventoryRepository.getAllItemsList()
            val detergent = items.firstOrNull { it.name.contains("моющ", ignoreCase = true) || it.name.contains("средств", ignoreCase = true) }
            if (detergent != null && detergent.quantity > 0) {
                val amount = when {
                    serviceType.contains("срочн", ignoreCase = true) -> 1.5
                    else -> 1.0
                }
                if (detergent.quantity >= amount) {
                    inventoryRepository.deductQuantity(detergent.id, amount)
                }
            }
        } catch (_: Exception) { }
    }
}
