package com.example.drycleaning.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.datastore.SessionManager
import com.example.drycleaning.data.entity.ServicePrice
import com.example.drycleaning.data.repository.AuthRepository
import com.example.drycleaning.data.repository.ServicePriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

/** ViewModel для настроек */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val servicePriceRepository: ServicePriceRepository
) : ViewModel() {

    val theme = sessionManager.theme
    val prices = servicePriceRepository.getAllPrices()
    val currentRole = authRepository.currentUserRole

    private val _backupResult = MutableStateFlow<String?>(null)
    val backupResult: StateFlow<String?> = _backupResult

    fun setTheme(theme: String) {
        viewModelScope.launch {
            sessionManager.setTheme(theme)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun updatePrice(servicePrice: ServicePrice) {
        viewModelScope.launch {
            servicePriceRepository.updatePrice(servicePrice)
        }
    }

    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            try {
                val dbFile = context.getDatabasePath("drycleaning_db")
                if (!dbFile.exists()) {
                    _backupResult.value = "База данных не найдена"
                    return@launch
                }
                val backupDir = File(context.getExternalFilesDir(null), "backup")
                backupDir.mkdirs()
                val backupFile = File(backupDir, "drycleaning_backup_${System.currentTimeMillis()}.db")
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
                _backupResult.value = "Резервная копия создана: ${backupFile.name}"
            } catch (e: Exception) {
                _backupResult.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun resetBackupResult() {
        _backupResult.value = null
    }
}
