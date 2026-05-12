package com.example.drycleaning.ui.clients

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drycleaning.databinding.FragmentClientDetailBinding
import com.example.drycleaning.ui.orders.OrderAdapter
import com.example.drycleaning.util.toDateString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент деталей клиента с историей заказов */
@AndroidEntryPoint
class ClientDetailFragment : Fragment() {

    private var _binding: FragmentClientDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientsViewModel by viewModels()
    private val args: ClientDetailFragmentArgs by navArgs()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderAdapter = OrderAdapter { order ->
            val action = ClientDetailFragmentDirections.actionClientDetailToOrderDetail(order.id)
            findNavController().navigate(action)
        }
        binding.rvClientOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClientOrders.adapter = orderAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getClientByIdFlow(args.clientId).collect { client ->
                        client?.let {
                            binding.tvClientName.text = it.fullName
                            binding.tvClientPhone.text = it.phone
                            binding.tvClientEmail.text = it.email.ifBlank { "—" }
                            binding.tvClientAddress.text = it.address.ifBlank { "—" }
                            binding.tvClientNotes.text = it.notes.ifBlank { "—" }
                            binding.tvCreatedAt.text = "Клиент с: ${it.createdAt.toDateString()}"

                            binding.btnEdit.setOnClickListener { _ ->
                                val action = ClientDetailFragmentDirections
                                    .actionClientDetailToCreateClient(it.id)
                                findNavController().navigate(action)
                            }

                            binding.btnDelete.setOnClickListener { _ ->
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Удалить клиента?")
                                    .setMessage("${it.fullName} и все его заказы будут удалены.")
                                    .setPositiveButton("Удалить") { _, _ ->
                                        viewModel.deleteClient(it)
                                        Snackbar.make(binding.root, "Клиент удалён", Snackbar.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.getClientOrders(args.clientId).collect { orders ->
                        orderAdapter.submitList(orders)
                        binding.tvOrdersTitle.text = "История заказов (${orders.size})"
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
