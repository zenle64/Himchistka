package com.example.drycleaning.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.drycleaning.databinding.FragmentReportsBinding
import com.example.drycleaning.util.toCurrencyString
import com.example.drycleaning.util.toDateString
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент отчётов и аналитики */
@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePickers()
        setupCharts()
        setupExportButtons()
        observeData()
    }

    private fun setupDatePickers() {
        binding.tvStartDate.setOnClickListener {
            showDatePicker("Начало") { viewModel.setStartDate(it) }
        }
        binding.tvEndDate.setOnClickListener {
            showDatePicker("Конец") { viewModel.setEndDate(it) }
        }
    }

    private fun showDatePicker(title: String, onDateSelected: (Long) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .build()
        picker.addOnPositiveButtonClickListener { onDateSelected(it) }
        picker.show(parentFragmentManager, "datePicker")
    }

    private fun setupCharts() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setEntryLabelTextSize(12f)
        }
        binding.barChart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
        }
    }

    private fun setupExportButtons() {
        binding.btnExportPdf.setOnClickListener {
            viewModel.exportToPdf(requireContext())
        }
        binding.btnExportOrdersCsv.setOnClickListener {
            viewModel.exportOrdersToCsv(requireContext())
        }
        binding.btnExportClientsCsv.setOnClickListener {
            viewModel.exportClientsToCsv(requireContext())
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.startDate.collect {
                        binding.tvStartDate.text = it.toDateString()
                    }
                }
                launch {
                    viewModel.endDate.collect {
                        binding.tvEndDate.text = it.toDateString()
                    }
                }
                launch {
                    viewModel.revenueForPeriod.collect {
                        binding.tvRevenue.text = it.toCurrencyString()
                    }
                }
                launch {
                    viewModel.orderCountForPeriod.collect {
                        binding.tvOrderCount.text = "Заказов: $it"
                    }
                }
                launch {
                    viewModel.averageCheck.collect {
                        binding.tvAverageCheck.text = "Средний чек: ${it.toCurrencyString()}"
                    }
                }
                launch {
                    viewModel.popularServices.collect { services ->
                        val entries = services.map { PieEntry(it.cnt.toFloat(), it.serviceType) }
                        if (entries.isNotEmpty()) {
                            val dataSet = PieDataSet(entries, "").apply {
                                colors = ColorTemplate.MATERIAL_COLORS.toList()
                                valueTextSize = 14f
                            }
                            binding.pieChart.data = PieData(dataSet)
                            binding.pieChart.invalidate()
                        }
                    }
                }
                launch {
                    viewModel.dailyRevenueData.collect { data ->
                        if (data.isNotEmpty()) {
                            val entries = data.mapIndexed { index, (_, revenue) ->
                                BarEntry(index.toFloat(), revenue.toFloat())
                            }
                            val labels = data.map { it.first }
                            val dataSet = BarDataSet(entries, "Выручка").apply {
                                colors = ColorTemplate.MATERIAL_COLORS.toList()
                                valueTextSize = 10f
                            }
                            binding.barChart.data = BarData(dataSet)
                            binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                            binding.barChart.xAxis.labelCount = labels.size.coerceAtMost(7)
                            binding.barChart.invalidate()
                        }
                    }
                }
                launch {
                    viewModel.exportResult.collect { result ->
                        result?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.resetExportResult()
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
