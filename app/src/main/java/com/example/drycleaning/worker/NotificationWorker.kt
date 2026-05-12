package com.example.drycleaning.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.drycleaning.MainActivity
import com.example.drycleaning.R
import com.example.drycleaning.data.repository.OrderRepository
import com.example.drycleaning.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker для периодической проверки и отправки уведомлений.
 * Проверяет готовые и просроченные заказы.
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderRepository: OrderRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        checkReadyOrders()
        checkOverdueOrders()
        return Result.success()
    }

    private suspend fun checkReadyOrders() {
        val readyOrders = orderRepository.getReadyOrders()
        if (readyOrders.isNotEmpty()) {
            showNotification(
                title = "Готовые заказы",
                message = "${readyOrders.size} заказ(ов) готовы к выдаче",
                notificationId = 1
            )
        }
    }

    private suspend fun checkOverdueOrders() {
        val overdueOrders = orderRepository.getOverdueOrders(System.currentTimeMillis())
        if (overdueOrders.isNotEmpty()) {
            showNotification(
                title = "Просроченные заказы",
                message = "${overdueOrders.size} заказ(ов) просрочены",
                notificationId = 2
            )
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }
}
