package com.example.drycleaning.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drycleaning.data.entity.InventoryItem
import com.example.drycleaning.databinding.FragmentInventoryBinding
import com.example.drycleaning.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout

/** Фрагмент управления складом */
@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter { item ->
            showEditDialog(item)
        }
        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventory.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddItem.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allItems.collect { items ->
                        adapter.submitList(items)
                        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.lowStockItems.collect { items ->
                        if (items.isNotEmpty()) {
                            val names = items.joinToString(", ") { it.name }
                            binding.tvLowStock.text = "Низкий остаток: $names"
                            binding.tvLowStock.visibility = View.VISIBLE
                        } else {
                            binding.tvLowStock.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showAddDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val etName = TextInputEditText(requireContext()).apply { hint = "Название" }
        val etQuantity = TextInputEditText(requireContext()).apply { hint = "Количество"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etMinQuantity = TextInputEditText(requireContext()).apply { hint = "Мин. остаток"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etPricePerUnit = TextInputEditText(requireContext()).apply { hint = "Цена за единицу"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val actvUnit = AutoCompleteTextView(requireContext()).apply {
            hint = "Единица"
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Constants.INVENTORY_UNITS))
        }

        layout.addView(etName)
        layout.addView(actvUnit)
        layout.addView(etQuantity)
        layout.addView(etMinQuantity)
        layout.addView(etPricePerUnit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавить материал")
            .setView(layout)
            .setPositiveButton("Добавить") { _, _ ->
                val name = etName.text.toString().trim()
                val unit = actvUnit.text.toString().trim()
                val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val minQty = etMinQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val price = etPricePerUnit.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    viewModel.saveItem(
                        InventoryItem(name = name, unit = unit, quantity = quantity, minQuantity = minQty, pricePerUnit = price)
                    )
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditDialog(item: InventoryItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.name)
            .setMessage("Остаток: ${item.quantity} ${item.unit}\nМин: ${item.minQuantity} ${item.unit}")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteItem(item)
                Snackbar.make(binding.root, "Удалено", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
