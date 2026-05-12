package com.example.drycleaning.ui.orders

import android.app.DatePickerDialog
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.data.entity.OrderStatus
import com.example.drycleaning.databinding.FragmentCreateOrderBinding
import com.example.drycleaning.util.Constants
import com.example.drycleaning.util.toCurrencyString
import com.example.drycleaning.util.toDateString
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

/** Фрагмент создания / редактирования заказа */
@AndroidEntryPoint
class CreateOrderFragment : Fragment() {

    private var _binding: FragmentCreateOrderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private val args: CreateOrderFragmentArgs by navArgs()

    private var receivedDate = System.currentTimeMillis()
    private var dueDate = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L // +3 дня
    private var editingOrder: Order? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupDatePickers()
        setupAutoPrice()
        setupSaveButton()
        observeResults()
        loadOrderIfEditing()
    }

    private fun setupDropdowns() {
        val itemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Constants.ITEM_TYPES)
        binding.actvItemType.setAdapter(itemAdapter)

        val serviceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Constants.SERVICE_TYPES)
        binding.actvServiceType.setAdapter(serviceAdapter)

        val statusNames = OrderStatus.entries.map {
            when (it) {
                OrderStatus.RECEIVED -> "Принят"
                OrderStatus.IN_PROGRESS -> "В работе"
                OrderStatus.READY -> "Готов"
                OrderStatus.DELIVERED -> "Выдан"
            }
        }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusNames)
        binding.actvStatus.setAdapter(statusAdapter)
        binding.actvStatus.setText("Принят", false)
    }

    private fun setupDatePickers() {
        binding.tvReceivedDate.text = receivedDate.toDateString()
        binding.tvDueDate.text = dueDate.toDateString()

        binding.tvReceivedDate.setOnClickListener { showDatePicker(true) }
        binding.tvDueDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isReceivedDate: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selected = Calendar.getInstance()
            selected.set(year, month, day)
            if (isReceivedDate) {
                receivedDate = selected.timeInMillis
                binding.tvReceivedDate.text = receivedDate.toDateString()
            } else {
                dueDate = selected.timeInMillis
                binding.tvDueDate.text = dueDate.toDateString()
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupAutoPrice() {
        binding.actvItemType.setOnItemClickListener { _, _, _, _ -> recalculatePrice() }
        binding.actvServiceType.setOnItemClickListener { _, _, _, _ -> recalculatePrice() }
    }

    private fun recalculatePrice() {
        val itemType = binding.actvItemType.text.toString()
        val serviceType = binding.actvServiceType.text.toString()
        if (itemType.isNotBlank() && serviceType.isNotBlank()) {
            viewModel.calculatePrice(itemType, serviceType)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val clientName = binding.etClientName.text.toString().trim()
            val clientPhone = binding.etClientPhone.text.toString().trim()
            val itemType = binding.actvItemType.text.toString().trim()
            val serviceType = binding.actvServiceType.text.toString().trim()
            val priceText = binding.etPrice.text.toString().trim()
            val comment = binding.etComment.text.toString().trim()
            val statusText = binding.actvStatus.text.toString().trim()

            if (clientName.isBlank() || clientPhone.isBlank() || itemType.isBlank() || serviceType.isBlank()) {
                Snackbar.make(binding.root, "Заполните все обязательные поля", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull() ?: 0.0
            val status = when (statusText) {
                "В работе" -> OrderStatus.IN_PROGRESS
                "Готов" -> OrderStatus.READY
                "Выдан" -> OrderStatus.DELIVERED
                else -> OrderStatus.RECEIVED
            }

            val order = Order(
                id = editingOrder?.id ?: 0,
                clientId = editingOrder?.clientId ?: 0,
                clientName = clientName,
                clientPhone = clientPhone,
                itemType = itemType,
                serviceType = serviceType,
                receivedDate = receivedDate,
                dueDate = dueDate,
                price = price,
                status = status,
                comment = comment,
                createdAt = editingOrder?.createdAt ?: System.currentTimeMillis()
            )

            if (editingOrder != null) {
                viewModel.updateOrder(order)
            } else {
                viewModel.saveOrder(order)
            }
        }
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.calculatedPrice.collect { price ->
                        if (price > 0 && editingOrder == null) {
                            binding.etPrice.setText(String.format("%.0f", price))
                            binding.tvAutoPrice.text = "Рекомендуемая цена: ${price.toCurrencyString()}"
                            binding.tvAutoPrice.visibility = View.VISIBLE
                        }
                    }
                }
                launch {
                    viewModel.saveResult.collect { result ->
                        result?.let {
                            it.onSuccess {
                                Snackbar.make(binding.root, "Заказ сохранён", Snackbar.LENGTH_SHORT).show()
                                viewModel.resetSaveResult()
                                findNavController().popBackStack()
                            }
                            it.onFailure { e ->
                                Snackbar.make(binding.root, "Ошибка: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                viewModel.resetSaveResult()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadOrderIfEditing() {
        val orderId = args.orderId
        if (orderId > 0) {
            binding.tvTitle.text = "Редактирование заказа"
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getOrderById(orderId).collect { order ->
                    order?.let {
                        editingOrder = it
                        binding.etClientName.setText(it.clientName)
                        binding.etClientPhone.setText(it.clientPhone)
                        binding.actvItemType.setText(it.itemType, false)
                        binding.actvServiceType.setText(it.serviceType, false)
                        binding.etPrice.setText(String.format("%.0f", it.price))
                        binding.etComment.setText(it.comment)
                        receivedDate = it.receivedDate
                        dueDate = it.dueDate
                        binding.tvReceivedDate.text = receivedDate.toDateString()
                        binding.tvDueDate.text = dueDate.toDateString()
                        val statusText = when (it.status) {
                            OrderStatus.RECEIVED -> "Принят"
                            OrderStatus.IN_PROGRESS -> "В работе"
                            OrderStatus.READY -> "Готов"
                            OrderStatus.DELIVERED -> "Выдан"
                        }
                        binding.actvStatus.setText(statusText, false)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
