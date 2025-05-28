package com.education.financetrackerrublik.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.databinding.FragmentStatisticsBinding
import com.education.financetrackerrublik.ui.adapter.CategoryStatisticsAdapter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.tabs.TabLayout
import androidx.core.graphics.toColorInt

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()
    private val adapter = CategoryStatisticsAdapter()
    private var currentType = TransactionType.EXPENSE
    private var currentPeriod = 1 // По умолчанию - месяц

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        viewModel.loadStatistics(currentType, currentPeriod)
    }

    private fun setupViews() {
        binding.categoriesList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentType = if (tab?.position == 0) {
                    TransactionType.EXPENSE
                } else {
                    TransactionType.INCOME
                }
                viewModel.loadStatistics(currentType, currentPeriod)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.tabLayout.apply {
            setTabTextColors(
                "#CCCCCC".toColorInt(),  // Цвет невыбранного таба
                android.graphics.Color.WHITE  // Цвет выбранного таба
            )
            setSelectedTabIndicatorColor(android.graphics.Color.WHITE)
        }
        binding.periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentPeriod = position
                viewModel.loadStatistics(currentType, currentPeriod)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupPieChart()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(android.graphics.Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.isEnabled = false
        }
    }

    private fun observeViewModel() {
        viewModel.statistics.observe(viewLifecycleOwner) { statistics ->
            val nonZeroStats = statistics.filter { it.amount > 0 }
            adapter.submitList(nonZeroStats)

            if (nonZeroStats.isNotEmpty()) {
                binding.pieChart.visibility = View.VISIBLE
                binding.emptyStateText.visibility = View.GONE
                updatePieChart(nonZeroStats)
            } else {
                binding.pieChart.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = if (currentType == TransactionType.EXPENSE) {
                    "Нет расходов за выбранный период"
                } else {
                    "Нет доходов за выбранный период"
                }
            }
        }
    }

    private fun updatePieChart(statistics: List<CategoryStatistics>) {
        val entries = statistics.map { stat ->
            PieEntry(stat.percentage, stat.category.name)
        }

        val colors = statistics.map { it.color }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = android.graphics.Color.WHITE
            valueFormatter = PercentFormatter()
            setDrawValues(true)
            valueLinePart1Length = 0f  // Убираем линии
            valueLinePart2Length = 0f
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE  // Проценты внутри секторов
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class PercentFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "%.1f%%".format(value)
    }
} 