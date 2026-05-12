package com.example.drycleaning.util

import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Форматирование даты из миллисекунд */
fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    return sdf.format(Date(this))
}

/** Форматирование даты и времени из миллисекунд */
fun Long.toDateTimeString(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
    return sdf.format(Date(this))
}

/** Форматирование цены в рублях */
fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    return format.format(this)
}

/** Получить начало текущего дня */
fun getStartOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** Получить начало текущего месяца */
fun getStartOfMonth(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** Получить русское название статуса заказа */
fun OrderStatus.toDisplayString(): String = when (this) {
    OrderStatus.RECEIVED -> "Принят"
    OrderStatus.IN_PROGRESS -> "В работе"
    OrderStatus.READY -> "Готов"
    OrderStatus.DELIVERED -> "Выдан"
}

/** Получить цвет статуса заказа */
fun OrderStatus.toColorRes(): Int = when (this) {
    OrderStatus.RECEIVED -> android.R.color.holo_blue_light
    OrderStatus.IN_PROGRESS -> android.R.color.holo_orange_light
    OrderStatus.READY -> android.R.color.holo_green_light
    OrderStatus.DELIVERED -> android.R.color.darker_gray
}

/** Форматирование номера заказа: ХЧ-2026-0001 */
fun Order.displayOrderNumber(): String {
    val year = Calendar.getInstance().apply { timeInMillis = createdAt }.get(Calendar.YEAR)
    return "ХЧ-$year-${id.toString().padStart(4, '0')}"
}
