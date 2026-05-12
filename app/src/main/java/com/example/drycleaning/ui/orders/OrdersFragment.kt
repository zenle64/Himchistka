package com.example.drycleaning.ui.orders

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drycleaning.R
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.databinding.FragmentOrdersBinding
import com.example.drycleaning.util.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        setupSort()
        setupPullToRefresh()
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
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val background = ColorDrawable(0xFFE53935.toInt())
            private val deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val order = adapter.currentList[position]
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удалить заказ?")
                    .setMessage("Заказ #${order.id} будет удалён.")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewModel.deleteOrder(order)
                        Snackbar.make(binding.root, "Заказ удалён", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, state: Int, active: Boolean) {
                val itemView = vh.itemView
                if (dX < 0) {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)
                    deleteIcon?.let {
                        val margin = (itemView.height - it.intrinsicHeight) / 2
                        val top = itemView.top + margin
                        val left = itemView.right - margin - it.intrinsicWidth
                        it.setBounds(left, top, left + it.intrinsicWidth, top + it.intrinsicHeight)
                        it.draw(c)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, state, active)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvOrders)
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

    private fun setupSort() {
        binding.btnSort.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add(0, 0, 0, "По дате создания")
            popup.menu.add(0, 1, 1, "По цене")
            popup.menu.add(0, 2, 2, "По статусу")
            popup.menu.add(0, 3, 3, "По дате выдачи")
            popup.setOnMenuItemClickListener { item ->
                val option = when (item.itemId) {
                    1 -> SortOption.PRICE
                    2 -> SortOption.STATUS
                    3 -> SortOption.DUE_DATE
                    else -> SortOption.DATE
                }
                viewModel.setSortOption(option)
                true
            }
            popup.show()
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.setSearchQuery(viewModel.searchQuery.value)
            binding.swipeRefresh.isRefreshing = false
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
                    binding.tvEmpty.text = if (orders.isEmpty()) "Нет заказов. Нажмите + чтобы создать." else ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
