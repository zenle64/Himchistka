package com.example.drycleaning.ui.clients

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.drycleaning.databinding.FragmentClientsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент списка клиентов */
@AndroidEntryPoint
class ClientsFragment : Fragment() {

    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientsViewModel by viewModels()
    private lateinit var adapter: ClientAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupPullToRefresh()
        setupFab()
        observeClients()
    }

    private fun setupRecyclerView() {
        adapter = ClientAdapter { client ->
            val action = ClientsFragmentDirections.actionClientsToClientDetail(client.id)
            findNavController().navigate(action)
        }
        binding.rvClients.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClients.adapter = adapter
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val background = ColorDrawable(0xFFE53935.toInt())
            private val deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val client = adapter.currentList[position]
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удалить клиента?")
                    .setMessage("${client.fullName} будет удалён.")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewModel.deleteClient(client)
                        Snackbar.make(binding.root, "Клиент удалён", Snackbar.LENGTH_SHORT).show()
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
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvClients)
    }

    private fun setupPullToRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.setSearchQuery(viewModel.searchQuery.value)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupFab() {
        binding.fabNewClient.setOnClickListener {
            findNavController().navigate(R.id.action_clients_to_createClient)
        }
    }

    private fun observeClients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.clients.collect { clients ->
                    adapter.submitList(clients)
                    binding.tvEmpty.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvEmpty.text = if (clients.isEmpty()) "Нет клиентов. Нажмите + чтобы добавить." else ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
