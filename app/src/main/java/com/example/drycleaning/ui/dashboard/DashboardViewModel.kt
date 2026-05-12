package com.example.drycleaning.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.drycleaning.data.repository.AuthRepository
import com.example.drycleaning.data.repository.OrderRepository
import com.example.drycleaning.util.getStartOfDay
import com.example.drycleaning.util.getStartOfMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** ViewModel главного экрана (Dashboard) */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val activeOrderCount = orderRepository.getActiveOrderCount()
    val completedOrderCount = orderRepository.getCompletedOrderCount()
    val dailyRevenue = orderRepository.getDailyRevenue(getStartOfDay())
    val monthlyRevenue = orderRepository.getMonthlyRevenue(getStartOfMonth())
    val userName = authRepository.currentUserName
}
