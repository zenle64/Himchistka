package com.example.drycleaning.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drycleaning.R
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.databinding.FragmentOrdersBinding
import com.example.drycleaning.util.toDisplayString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент списка заказов */
@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilter()
        setupFab()
        observeOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter { order ->
            val action = OrdersFragmentDirections.actionOrdersToOrderDetail(order.id)
            findNavController().navigate(action)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupFilter() {
        val statusNames = listOf("Все") + OrderStatus.entries.map { it.toDisplayString() }
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusNames)
        binding.spinnerStatus.setAdapter(filterAdapter)
        binding.spinnerStatus.setOnItemClickListener { _, _, position, _ ->
            val status = if (position == 0) null else OrderStatus.entries[position - 1]
            viewModel.setStatusFilter(status)
        }
    }

    private fun setupFab() {
        binding.fabNewOrder.setOnClickListener {
            findNavController().navigate(R.id.action_orders_to_createOrder)
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collect { orders ->
                    adapter.submitList(orders)
                    binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
