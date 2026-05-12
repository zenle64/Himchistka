package com.example.drycleaning.ui.reports

import android.app.DatePickerDialog
import android.graphics.Color
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

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
        setupExportButton()
        observeData()
    }

    private fun setupDatePickers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.startDate.collect { date ->
                binding.tvStartDate.text = date.toDateString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.endDate.collect { date ->
                binding.tvEndDate.text = date.toDateString()
            }
        }

        binding.tvStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selected = Calendar.getInstance().apply { set(y, m, d) }
                viewModel.setStartDate(selected.timeInMillis)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selected = Calendar.getInstance().apply { set(y, m, d) }
                viewModel.setEndDate(selected.timeInMillis)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupExportButton() {
        binding.btnExportPdf.setOnClickListener {
            viewModel.exportToPdf(requireContext())
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getRevenueForPeriod().collect { revenue ->
                        binding.tvRevenue.text = revenue.toCurrencyString()
                    }
                }
                launch {
                    viewModel.getOrderCountForPeriod().collect { count ->
                        binding.tvOrderCount.text = "Заказов: $count"
                    }
                }
                launch {
                    viewModel.popularServices.collect { services ->
                        if (services.isNotEmpty()) {
                            setupPieChart(services.map { it.serviceType to it.cnt })
                            setupBarChart(services.map { it.serviceType to it.cnt })
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

    private fun setupPieChart(data: List<Pair<String, Int>>) {
        val entries = data.map { PieEntry(it.second.toFloat(), it.first) }
        val dataSet = PieDataSet(entries, "Услуги").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }
        binding.pieChart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChart(data: List<Pair<String, Int>>) {
        val entries = data.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second.toFloat())
        }
        val dataSet = BarDataSet(entries, "Количество заказов").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }
        binding.barChart.apply {
            this.data = BarData(dataSet)
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(data.map { it.first })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
