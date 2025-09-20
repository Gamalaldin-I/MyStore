package com.example.htopstore.ui.bills

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityBillsBinding
import com.example.htopstore.ui.billDetails.BillDetailsActivity
import com.example.htopstore.ui.widgets.DatePickerFragment
import com.example.htopstore.util.adapters.BillsAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class BillsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillsBinding
    private lateinit var adapter: BillsAdapter
    private val viewModel: BillViewModel by viewModels()

    companion object {
        private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        controlDate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBills()
    }

    private fun setupRecyclerView() {
        adapter = BillsAdapter(emptyList()) { saleId ->
            navigateToBillDetails(saleId)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        // Observe bills data
        viewModel.bills.observe(this) { bills ->
            adapter.updateData(bills)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            //binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe total sum
        viewModel.totalSum.observe(this) { sum ->
            updateSum(sum)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun controlDate() {
        setupDatePicker(binding.since, isSince = true)
        setupDatePicker(binding.to, isSince = false)

        binding.reset.setOnClickListener {
            binding.since.text = "Since"
            binding.to.text = "To"
            viewModel.resetFilters()
        }
    }

    private fun setupDatePicker(view: android.widget.TextView, isSince: Boolean) {
        view.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                val pickedDate = LocalDate.of(year, month, day)

                val errorMessage = if (isSince) {
                    binding.since.text = pickedDate.format(displayFormatter)
                    viewModel.setSinceDate(pickedDate)
                } else {
                    binding.to.text = pickedDate.format(displayFormatter)
                    viewModel.setToDate(pickedDate)
                }

                errorMessage?.let { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    // Reset the text if there was an error
                    if (isSince) {
                        binding.since.text = "Since"
                    } else {
                        binding.to.text = "To"
                    }
                }
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    private fun updateSum(sum: Double) {
        binding.total.text = sum.toString()
        binding.totalIcon.animate().apply {
            duration = 1000
            rotationBy(360f)
        }
    }

    private fun navigateToBillDetails(saleId: String) {
        val intent = Intent(this, BillDetailsActivity::class.java).apply {
            putExtra("saleId", saleId)
        }
        startActivity(intent)
    }
}