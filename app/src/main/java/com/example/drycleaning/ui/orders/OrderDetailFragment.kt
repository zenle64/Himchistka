package com.example.drycleaning.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.drycleaning.databinding.FragmentOrderDetailBinding
import com.example.drycleaning.util.toCurrencyString
import com.example.drycleaning.util.toColorRes
import com.example.drycleaning.util.toDateString
import com.example.drycleaning.util.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент деталей заказа */
@AndroidEntryPoint
class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private val args: OrderDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getOrderById(args.orderId).collect { order ->
                    order?.let {
                        binding.tvOrderNumber.text = "Заказ #${it.id}"
                        binding.tvClientName.text = it.clientName
                        binding.tvClientPhone.text = it.clientPhone
                        binding.tvItemType.text = it.itemType
                        binding.tvServiceType.text = it.serviceType
                        binding.tvReceivedDate.text = it.receivedDate.toDateString()
                        binding.tvDueDate.text = it.dueDate.toDateString()
                        binding.tvPrice.text = it.price.toCurrencyString()
                        binding.tvStatus.text = it.status.toDisplayString()
                        binding.tvStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), it.status.toColorRes())
                        )
                        binding.tvComment.text = it.comment.ifBlank { "—" }

                        binding.btnEdit.setOnClickListener { _ ->
                            val action = OrderDetailFragmentDirections.actionOrderDetailToCreateOrder(it.id)
                            findNavController().navigate(action)
                        }

                        binding.btnDelete.setOnClickListener { _ ->
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Удалить заказ?")
                                .setMessage("Заказ #${it.id} будет удалён безвозвратно.")
                                .setPositiveButton("Удалить") { _, _ ->
                                    viewModel.deleteOrder(it)
                                    Snackbar.make(binding.root, "Заказ удалён", Snackbar.LENGTH_SHORT).show()
                                    findNavController().popBackStack()
                                }
                                .setNegativeButton("Отмена", null)
                                .show()
                        }
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
