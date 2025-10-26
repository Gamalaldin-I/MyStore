package com.example.htopstore.ui.analysis

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.useCase.localize.NAE.digit
import com.example.htopstore.databinding.FragmentAccountantBinding
import com.example.htopstore.ui.widgets.DatePickerFragment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class AccountantFragment : Fragment() {
    private lateinit var binding: FragmentAccountantBinding
    private val vm: AnalysisViewModel by activityViewModels()
    private var from = ""
    private var to = ""
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val buttonFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountantBinding.inflate(inflater, container, false)
        // Hide report initially
        binding.accountBrief.root.visibility = View.GONE
        binding.exportBtn.visibility = View.GONE

        setControllers()
        observe()
        return binding.root
    }

    private fun setControllers() {
        // Reset button
        binding.reset.setOnClickListener {
            onReset()
        }

        // Calculate button
        binding.calculate.setOnClickListener {
            onCalc()
        }

        // From date picker
        binding.PickFrom.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                from = LocalDate.of(year, month, day).format(displayFormatter)
                binding.PickFrom.text = LocalDate.of(year, month, day).format(buttonFormatter)
                uncheckAllChips()
            }
            datePicker.show(parentFragmentManager, "datePickerFrom")
        }

        // To date picker
        binding.pickTo.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                to = LocalDate.of(year, month, day).format(displayFormatter)
                binding.pickTo.text = LocalDate.of(year, month, day).format(buttonFormatter)
                uncheckAllChips()
            }
            datePicker.show(parentFragmentManager, "datePickerTo")
        }

        // Quick select chips
        binding.chipToday.setOnClickListener {
            setDateRange(LocalDate.now(), LocalDate.now())
        }

        binding.chipWeek.setOnClickListener {
            val today = LocalDate.now()
            val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            setDateRange(startOfWeek, endOfWeek)
        }

        binding.chipMonth.setOnClickListener {
            val today = LocalDate.now()
            val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
            val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
            setDateRange(startOfMonth, endOfMonth)
        }

        binding.chipYear.setOnClickListener {
            val today = LocalDate.now()
            val startOfYear = today.with(TemporalAdjusters.firstDayOfYear())
            val endOfYear = today.with(TemporalAdjusters.lastDayOfYear())
            setDateRange(startOfYear, endOfYear)
        }

        // Export button
        binding.exportBtn.setOnClickListener {
            onExport()
        }
    }

    private fun setDateRange(fromDate: LocalDate, toDate: LocalDate) {
        from = fromDate.format(displayFormatter)
        to = toDate.format(displayFormatter)
        binding.PickFrom.text = fromDate.format(buttonFormatter)
        binding.pickTo.text = toDate.format(buttonFormatter)
    }

    private fun uncheckAllChips() {
        binding.quickSelectChips.clearCheck()
    }

    @SuppressLint("SetTextI18n")
    private fun onReset() {
        // Reset dates
        from = ""
        to = ""
        binding.pickTo.text = "Select End Date"
        binding.PickFrom.text = "Select Start Date"

        // Hide report and export button
        binding.accountBrief.root.visibility = View.GONE
        binding.exportBtn.visibility = View.GONE

        // Uncheck all chips
        uncheckAllChips()
    }

    private fun onCalc() {
        // Validate dates
        if (to.isEmpty() || from.isEmpty()) {
            showError("Please select both start and end dates")
            return
        }

        if (from > to) {
            showError("Start date must be before end date")
            return
        }

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.accountBrief.root.visibility = View.GONE

        // Fetch data
        vm.getBrief(from, to)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun observe() {
        vm.brief.observe(viewLifecycleOwner) { briefData ->
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE

            // Extract values
            val sales = briefData[0]
            val expenses = briefData[1]
            val profit = briefData[2]
            val netProfitv = profit - expenses
            val buyingValue = sales - profit

            // Update UI
            binding.accountBrief.apply {
                totalSales.text = sales.digit(1)
                totalExpenses.text = expenses.digit(1)
                totalProfit.text = profit.digit(1)
                ValueOfBuying.text = buyingValue.digit(1)
                netProfit.text = netProfitv.digit(1)

                // Update date range text in header
                dateRangeText.text = "${binding.PickFrom.text} - ${binding.pickTo.text}"

                // Show report
                root.visibility = View.VISIBLE
            }

            // Show export button
            binding.exportBtn.visibility = View.VISIBLE

            // Scroll to report
            binding.scroller.post {
                binding.scroller.smoothScrollTo(0, binding.accountBrief.root.top)
            }
        }
    }

    private fun onExport() {
        // TODO: Implement export functionality
        // This could export to PDF, CSV, or share the report
        Toast.makeText(requireContext(), "Export functionality coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}