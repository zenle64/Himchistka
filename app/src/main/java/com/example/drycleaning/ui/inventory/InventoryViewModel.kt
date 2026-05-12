package com.example.drycleaning.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.InventoryItem
import com.example.drycleaning.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel для управления складом */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allItems = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            inventoryRepository.getAllItems()
        } else {
            inventoryRepository.searchItems(query)
        }
    }

    val lowStockItems = inventoryRepository.getLowStockItems()

    private val _saveResult = MutableStateFlow<Result<Long>?>(null)
    val saveResult: StateFlow<Result<Long>?> = _saveResult

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                val id = inventoryRepository.insertItem(item)
                _saveResult.value = Result.success(id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                inventoryRepository.updateItem(item)
                _saveResult.value = Result.success(item.id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            inventoryRepository.deleteItem(item)
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }
}
