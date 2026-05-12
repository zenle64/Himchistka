package com.example.drycleaning.ui.orders

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
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
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.databinding.FragmentOrderDetailBinding
import com.example.drycleaning.util.displayOrderNumber
import com.example.drycleaning.util.toCurrencyString
import com.example.drycleaning.util.toColorRes
import com.example.drycleaning.util.toDateString
import com.example.drycleaning.util.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
                        binding.tvOrderNumber.text = it.displayOrderNumber()
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
                                .setMessage("${it.displayOrderNumber()} будет удалён безвозвратно.")
                                .setPositiveButton("Удалить") { _, _ ->
                                    viewModel.deleteOrder(it)
                                    Snackbar.make(binding.root, "Заказ удалён", Snackbar.LENGTH_SHORT).show()
                                    findNavController().popBackStack()
                                }
                                .setNegativeButton("Отмена", null)
                                .show()
                        }

                        binding.btnPrintReceipt.setOnClickListener { _ ->
                            printReceipt(it)
                        }
                    }
                }
            }
        }
    }

    private fun printReceipt(order: Order) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val linePaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
            }

            var y = 30f
            canvas.drawText("ХИМЧИСТКА", 150f, y, titlePaint)
            y += 20f
            canvas.drawText("Квитанция", 150f, y, titlePaint.apply { textSize = 12f })
            y += 20f
            canvas.drawLine(20f, y, 280f, y, linePaint)
            y += 15f

            canvas.drawText("Номер: ${order.displayOrderNumber()}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Клиент: ${order.clientName}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Телефон: ${order.clientPhone}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Изделие: ${order.itemType}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Услуга: ${order.serviceType}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Приём: ${order.receivedDate.toDateString()}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Выдача: ${order.dueDate.toDateString()}", 20f, y, textPaint)
            y += 18f
            canvas.drawText("Статус: ${order.status.toDisplayString()}", 20f, y, textPaint)
            y += 25f
            canvas.drawLine(20f, y, 280f, y, linePaint)
            y += 20f

            val pricePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
            }
            canvas.drawText("Итого: ${order.price.toCurrencyString()}", 20f, y, pricePaint)

            if (order.comment.isNotBlank()) {
                y += 25f
                canvas.drawText("Комментарий: ${order.comment}", 20f, y, textPaint)
            }

            document.finishPage(page)

            val dir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "receipts")
            dir.mkdirs()
            val fileName = "receipt_${order.id}_${System.currentTimeMillis()}.pdf"
            val file = File(dir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            Snackbar.make(binding.root, "Квитанция сохранена: $fileName", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Ошибка: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
