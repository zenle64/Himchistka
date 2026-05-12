package com.example.drycleaning.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.drycleaning.R
import com.example.drycleaning.databinding.FragmentDashboardBinding
import com.example.drycleaning.util.toCurrencyString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент главного экрана (Dashboard) */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupButtons()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.activeOrderCount.collect { count ->
                        binding.tvActiveOrders.text = count.toString()
                    }
                }
                launch {
                    viewModel.completedOrderCount.collect { count ->
                        binding.tvCompletedOrders.text = count.toString()
                    }
                }
                launch {
                    viewModel.dailyRevenue.collect { revenue ->
                        binding.tvDailyRevenue.text = revenue.toCurrencyString()
                    }
                }
                launch {
                    viewModel.monthlyRevenue.collect { revenue ->
                        binding.tvMonthlyRevenue.text = revenue.toCurrencyString()
                    }
                }
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvWelcome.text = "Добро пожаловать, $name!"
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnNewOrder.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_createOrder)
        }
        binding.btnClients.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_clients)
        }
        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_reports)
        }
        binding.btnInventory.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_inventory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
