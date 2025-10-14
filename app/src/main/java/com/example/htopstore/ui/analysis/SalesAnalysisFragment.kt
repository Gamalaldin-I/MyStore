package com.example.htopstore.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.model.ExpensesWithCategory
import com.example.domain.model.SalesProfitByPeriod
import com.example.domain.util.DateHelper.DAY
import com.example.domain.util.DateHelper.MONTH
import com.example.domain.util.DateHelper.WEEK
import com.example.domain.util.DateHelper.YEAR
import com.example.htopstore.databinding.FragmentSalesAnalysisBinding
import com.example.htopstore.util.Visualiser.drawLineChart
import com.example.htopstore.util.Visualiser.drawPieChart
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class SalesAnalysisFragment : Fragment() {

    private lateinit var binding: FragmentSalesAnalysisBinding
    private val vm: AnalysisViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSalesAnalysisBinding.inflate(inflater, container, false)

        val autoComplete = binding.autoComplete
        val options = listOf(DAY, WEEK, MONTH, YEAR)

        // Default period = WEEK
        onSelect(WEEK)
        autoComplete.setAdapter(AutoCompleteHelper.getDurationAdapter(requireContext()))
        autoComplete.setText(WEEK, false)

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            onSelect(options[position])
        }

        observe()
        return binding.root
    }

    private fun onSelect(period: String) {
        vm.apply {
            getSalesAndProfitByPeriod(period)
            getTheAVGSales(period)
            getTheNumOfSales(period)
            getTheTotalOfSalesValue(period)
            getTheBestPeriod(period)
            getTheProfit(period)
            getTheBestDayOfWeek(period)
            getExpensesWithCategory(period)
        }
    }

    private fun observe() {
        vm.salesAndProfitByPeriod.observe(viewLifecycleOwner) {
            it?.visualise()
        }

        vm.avgOfSales.observe(viewLifecycleOwner) {
            binding.numbers.avgOfSell.text = it?.toInt()?.toString() ?: "0"
        }

        vm.numberOfSales.observe(viewLifecycleOwner) {
            binding.numbers.salesOps.text = it?.toInt()?.toString() ?: "0"
        }

        vm.totalSales.observe(viewLifecycleOwner) {
            binding.numbers.totalSales.text = it?.toInt()?.toString() ?: "0"
        }

        vm.theHoursWithHighestSales.observe(viewLifecycleOwner) {
            binding.numbers.topHour.text = it ?: "-"
        }

        vm.profit.observe(viewLifecycleOwner) {
            binding.numbers.profit.text = it?.toInt()?.toString() ?: "0"
        }

        vm.theDaysWithHighestSales.observe(viewLifecycleOwner) {
            binding.numbers.topDay.text = it ?: "-"
        }

        vm.expensesWithCategory.observe(viewLifecycleOwner) { expenses ->
            updateExpenses(expenses)
        }
    }

    private fun updateExpenses(expenses: List<ExpensesWithCategory>?) {
        if (expenses.isNullOrEmpty()) {
            // Hide entire section if there are no expenses
            binding.expensesLo.visibility = View.GONE
            binding.expensesGrid.grid.visibility = View.GONE
            listOf(
                binding.expensesGrid.c0,
                binding.expensesGrid.c1,
                binding.expensesGrid.c2,
                binding.expensesGrid.c3
            ).forEach { it.visibility = View.GONE }
            return
        }

        binding.expensesLo.visibility = View.VISIBLE
        binding.expensesGrid.grid.visibility = View.VISIBLE

        val categories = listOf(
            Triple(binding.expensesGrid.firstCat, binding.expensesGrid.firstCatVal, binding.expensesGrid.c0),
            Triple(binding.expensesGrid.secondCat, binding.expensesGrid.secondCatVal, binding.expensesGrid.c1),
            Triple(binding.expensesGrid.thirdCat, binding.expensesGrid.thirdCatVal, binding.expensesGrid.c2),
            Triple(binding.expensesGrid.fourthCat, binding.expensesGrid.fourthCatVal, binding.expensesGrid.c3)
        )

        // Hide all by default
        categories.forEach { (_, _, layout) -> layout.visibility = View.GONE }

        // Fill available categories
        expenses.forEachIndexed { index, item ->
            if (index < categories.size) {
                val (catName, catVal, catLayout) = categories[index]
                catName.text = item.category
                catVal.text = item.amount.toInt().toString()
                catLayout.visibility = View.VISIBLE
            }
        }

        drawPieChart(binding.expensesChart, expenses.mapToListOfPieEntry(), "Expenses")
    }

    private fun List<SalesProfitByPeriod>.visualise() {
        if (isEmpty()) {
            binding.curves.lineChart.visibility = View.GONE
            binding.numbers.grid.visibility = View.GONE
            binding.hint.visibility = View.VISIBLE
            return
        }

        val sales = map { it.total ?: 0f }
        val profit = map { it.profit ?: 0f }
        val labels = map { it.period?.let(::formatPeriodLabel).orEmpty() }

        binding.curves.lineChart.visibility = View.VISIBLE
        binding.numbers.grid.visibility = View.VISIBLE
        binding.hint.visibility = View.GONE

        drawLineChart(binding.curves.lineChart, sales, profit, labels)
    }

    private fun formatPeriodLabel(period: String): String {
        return when {
            period.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                val (year, month, day) = period.split("-")
                "$day/$month"
            }
            period.matches(Regex("\\d{4}-\\d{2}")) -> {
                val monthNames = listOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
                val month = period.split("-")[1].toIntOrNull()?.let { monthNames.getOrNull(it - 1) } ?: "-"
                month
            }
            period.matches(Regex("\\d{2}:\\d{2}.*")) -> period.substring(0, 5)
            else -> period
        }
    }

    private fun List<ExpensesWithCategory>.mapToListOfPieEntry(): List<PieEntry> {
        return map {
            val expense = abs(it.amount)
            PieEntry(expense, it.category)
        }
    }
}
