package com.example.drycleaning.util

/** Константы приложения */
object Constants {
    /** Типы изделий */
    val ITEM_TYPES = listOf(
        "Пальто", "Костюм", "Платье", "Куртка",
        "Рубашка", "Брюки", "Юбка", "Пуховик"
    )

    /** Типы услуг */
    val SERVICE_TYPES = listOf(
        "Химчистка", "Глажка", "Срочная химчистка"
    )

    /** Единицы измерения для склада */
    val INVENTORY_UNITS = listOf("л", "кг", "шт", "уп")

    /** Канал уведомлений */
    const val NOTIFICATION_CHANNEL_ID = "drycleaning_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Уведомления химчистки"
}
