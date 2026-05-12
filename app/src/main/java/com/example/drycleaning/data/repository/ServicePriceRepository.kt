package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.ServicePriceDao
import com.example.drycleaning.data.entity.ServicePrice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с прайс-листом.
 * Управление ценами на услуги, автоматический расчёт стоимости.
 */
@Singleton
class ServicePriceRepository @Inject constructor(
    private val servicePriceDao: ServicePriceDao
) {
    fun getAllPrices(): Flow<List<ServicePrice>> = servicePriceDao.getAllPrices()

    fun getAllItemTypes(): Flow<List<String>> = servicePriceDao.getAllItemTypes()

    fun getAllServiceTypes(): Flow<List<String>> = servicePriceDao.getAllServiceTypes()

    suspend fun getPrice(itemType: String, serviceType: String): ServicePrice? =
        servicePriceDao.getPrice(itemType, serviceType)

    suspend fun calculatePrice(itemType: String, serviceType: String): Double {
        return servicePriceDao.getPrice(itemType, serviceType)?.price ?: 0.0
    }

    suspend fun insertPrice(servicePrice: ServicePrice): Long = servicePriceDao.insert(servicePrice)

    suspend fun updatePrice(servicePrice: ServicePrice) = servicePriceDao.update(servicePrice)

    suspend fun deletePrice(servicePrice: ServicePrice) = servicePriceDao.delete(servicePrice)

    suspend fun ensureDefaultPrices() {
        val itemTypes = listOf("Пальто", "Костюм", "Платье", "Куртка", "Рубашка", "Брюки", "Юбка", "Пуховик")
        val services = mapOf(
            "Химчистка" to mapOf(
                "Пальто" to 1500.0, "Костюм" to 1800.0, "Платье" to 1200.0,
                "Куртка" to 1000.0, "Рубашка" to 500.0, "Брюки" to 600.0,
                "Юбка" to 700.0, "Пуховик" to 2000.0
            ),
            "Глажка" to mapOf(
                "Пальто" to 500.0, "Костюм" to 600.0, "Платье" to 400.0,
                "Куртка" to 350.0, "Рубашка" to 200.0, "Брюки" to 250.0,
                "Юбка" to 300.0, "Пуховик" to 500.0
            ),
            "Срочная химчистка" to mapOf(
                "Пальто" to 2500.0, "Костюм" to 2800.0, "Платье" to 2000.0,
                "Куртка" to 1700.0, "Рубашка" to 900.0, "Брюки" to 1000.0,
                "Юбка" to 1100.0, "Пуховик" to 3200.0
            )
        )
        for ((serviceType, prices) in services) {
            for ((itemType, price) in prices) {
                val existing = servicePriceDao.getPrice(itemType, serviceType)
                if (existing == null) {
                    servicePriceDao.insert(ServicePrice(itemType = itemType, serviceType = serviceType, price = price))
                }
            }
        }
    }
}
