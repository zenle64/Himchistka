package com.example.drycleaning.ui.reports

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.repository.ClientRepository
import com.example.drycleaning.data.repository.OrderRepository
import com.example.drycleaning.util.toDateString
import com.example.drycleaning.util.toCurrencyString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** ViewModel для отчётов и аналитики */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow(getDefaultStartDate())
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val endDate: StateFlow<Long> = _endDate

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult

    val popularServices = orderRepository.getPopularServices()

    @OptIn(ExperimentalCoroutinesApi::class)
    val revenueForPeriod = combine(_startDate, _endDate) { start, end -> start to end }
        .flatMapLatest { (start, end) -> orderRepository.getRevenueForPeriod(start, end) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val orderCountForPeriod = combine(_startDate, _endDate) { start, end -> start to end }
        .flatMapLatest { (start, end) -> orderRepository.getOrderCountForPeriod(start, end) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val averageCheck = combine(_startDate, _endDate) { start, end -> start to end }
        .flatMapLatest { (start, end) -> orderRepository.getAverageCheckForPeriod(start, end) }

    private val _dailyRevenueData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val dailyRevenueData: StateFlow<List<Pair<String, Double>>> = _dailyRevenueData

    init {
        loadDailyRevenueData()
    }

    fun setStartDate(date: Long) {
        _startDate.value = date
        loadDailyRevenueData()
    }

    fun setEndDate(date: Long) {
        _endDate.value = date
        loadDailyRevenueData()
    }

    private fun loadDailyRevenueData() {
        viewModelScope.launch {
            try {
                val orders = orderRepository.getDeliveredOrdersForPeriod(_startDate.value, _endDate.value)
                val sdf = SimpleDateFormat("dd.MM", Locale("ru"))
                val grouped = orders.groupBy { sdf.format(Date(it.createdAt)) }
                    .map { (date, orderList) -> date to orderList.sumOf { it.price } }
                _dailyRevenueData.value = grouped
            } catch (_: Exception) {
                _dailyRevenueData.value = emptyList()
            }
        }
    }

    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            try {
                val revenue = revenueForPeriod.first()
                val orderCount = orderCountForPeriod.first()
                val services = orderRepository.getPopularServices().first()

                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                val startStr = sdf.format(Date(_startDate.value))
                val endStr = sdf.format(Date(_endDate.value))

                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val titlePaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 20f
                    isFakeBoldText = true
                }
                val textPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 14f
                }

                var y = 50f
                canvas.drawText("Отчёт химчистки", 50f, y, titlePaint)
                y += 30f
                canvas.drawText("Период: $startStr — $endStr", 50f, y, textPaint)
                y += 25f
                canvas.drawText("Выручка: ${String.format("%.2f", revenue)} ₽", 50f, y, textPaint)
                y += 25f
                canvas.drawText("Количество заказов: $orderCount", 50f, y, textPaint)
                y += 35f
                canvas.drawText("Популярные услуги:", 50f, y, titlePaint)
                y += 25f
                for (service in services) {
                    canvas.drawText("  ${service.serviceType}: ${service.cnt} заказов", 50f, y, textPaint)
                    y += 20f
                }

                document.finishPage(page)

                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
                dir.mkdirs()
                val fileName = "report_${System.currentTimeMillis()}.pdf"
                val file = File(dir, fileName)
                FileOutputStream(file).use { document.writeTo(it) }
                document.close()

                _exportResult.value = file.absolutePath
            } catch (e: Exception) {
                _exportResult.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun exportOrdersToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val orders = orderRepository.getDeliveredOrdersForPeriod(_startDate.value, _endDate.value)
                val allOrders = orderRepository.getAllOrders().first()

                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "export")
                dir.mkdirs()
                val fileName = "orders_${System.currentTimeMillis()}.csv"
                val file = File(dir, fileName)

                file.bufferedWriter().use { writer ->
                    writer.write("ID;Клиент;Телефон;Изделие;Услуга;Дата приёма;Дата выдачи;Цена;Статус;Комментарий")
                    writer.newLine()
                    for (order in allOrders) {
                        writer.write("${order.id};${order.clientName};${order.clientPhone};${order.itemType};${order.serviceType};${order.receivedDate.toDateString()};${order.dueDate.toDateString()};${order.price};${order.status};${order.comment}")
                        writer.newLine()
                    }
                }

                _exportResult.value = "CSV сохранён: $fileName"
            } catch (e: Exception) {
                _exportResult.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun exportClientsToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val clients = clientRepository.getAllClients().first()

                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "export")
                dir.mkdirs()
                val fileName = "clients_${System.currentTimeMillis()}.csv"
                val file = File(dir, fileName)

                file.bufferedWriter().use { writer ->
                    writer.write("ID;ФИО;Телефон;Email;Адрес;Заметки;Дата регистрации")
                    writer.newLine()
                    for (client in clients) {
                        writer.write("${client.id};${client.fullName};${client.phone};${client.email};${client.address};${client.notes};${client.createdAt.toDateString()}")
                        writer.newLine()
                    }
                }

                _exportResult.value = "CSV сохранён: $fileName"
            } catch (e: Exception) {
                _exportResult.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun resetExportResult() {
        _exportResult.value = null
    }

    private fun getDefaultStartDate(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return cal.timeInMillis
    }
}
