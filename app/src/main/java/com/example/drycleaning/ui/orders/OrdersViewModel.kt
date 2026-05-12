package com.example.drycleaning.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.data.repository.OrderRepository
import com.example.drycleaning.data.repository.ServicePriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel для управления заказами */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val servicePriceRepository: ServicePriceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow<OrderStatus?>(null)
    val statusFilter: StateFlow<OrderStatus?> = _statusFilter

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
                    orderRepository.getAllOrders()
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

    fun calculatePrice(itemType: String, serviceType: String) {
        viewModelScope.launch {
            _calculatedPrice.value = servicePriceRepository.calculatePrice(itemType, serviceType)
        }
    }

    fun saveOrder(order: Order) {
        viewModelScope.launch {
            try {
                val id = orderRepository.insertOrder(order)
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
}
