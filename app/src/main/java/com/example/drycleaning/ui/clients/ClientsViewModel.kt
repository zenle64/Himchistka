package com.example.drycleaning.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.Client
import com.example.drycleaning.data.repository.ClientRepository
import com.example.drycleaning.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel для управления клиентами */
@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _saveResult = MutableStateFlow<Result<Long>?>(null)
    val saveResult: StateFlow<Result<Long>?> = _saveResult

    @OptIn(ExperimentalCoroutinesApi::class)
    val clients = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            clientRepository.getAllClients()
        } else {
            clientRepository.searchClients(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveClient(client: Client) {
        viewModelScope.launch {
            try {
                val id = clientRepository.insertClient(client)
                _saveResult.value = Result.success(id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            try {
                clientRepository.updateClient(client)
                _saveResult.value = Result.success(client.id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
        }
    }

    fun getClientByIdFlow(id: Long) = clientRepository.getClientByIdFlow(id)

    fun getClientOrders(clientId: Long) = orderRepository.getOrdersByClient(clientId)

    fun resetSaveResult() {
        _saveResult.value = null
    }
}
