package com.example.htopstore.ui.sales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.SoldProduct
import com.example.htopstore.databinding.ActivityReturnsBinding
import com.example.htopstore.ui.billDetails.BillDetailsActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.ui.widgets.DatePickerFragment
import com.example.htopstore.util.adapters.ReturnsAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReturnsBinding
    private lateinit var adapter: ReturnsAdapter
    private val viewModel: SalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReturnsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupControllers()

        // Set initial state
        binding.allSales.isChecked = true
    }

    private fun setupRecyclerView() {
        adapter = ReturnsAdapter(ArrayList()) { soldProduct ->
            onItemClick(soldProduct)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        // Observe sales data
        viewModel.salesData.observe(this) { salesList ->
            adapter.updateData(salesList)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
           // binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Disable chips while loading
            binding.allSales.isEnabled = !isLoading
            binding.sold.isEnabled = !isLoading
            binding.returns.isEnabled = !isLoading
            binding.selectDay.isEnabled = !isLoading
            binding.reset.isEnabled = !isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe current filter
        viewModel.currentFilter.observe(this) { filter ->
            updateFilterUI(filter)
        }

        // Observe item count
        viewModel.itemCount.observe(this) { count ->
            //binding.rowNum?.text = count.toString()
        }
    }

    private fun setupControllers() {
        setupChipControllers()
        setupDateControllers()
    }

    private fun setupChipControllers() {
        binding.allSales.setOnClickListener {
            updateChipSelection(SalesViewModel.Companion.SalesType.ALL_SALES)
        }

        binding.sold.setOnClickListener {
            updateChipSelection(SalesViewModel.Companion.SalesType.SOLD)
        }

        binding.returns.setOnClickListener {
            updateChipSelection(SalesViewModel.Companion.SalesType.RETURNS)
        }
    }

    private fun setupDateControllers() {
        binding.selectDay.setOnClickListener {
            showDatePicker()
        }

        binding.reset.setOnClickListener {
            resetFilters()
        }
    }

    private fun updateChipSelection(salesType: SalesViewModel.Companion.SalesType) {
        // Update chip states
        binding.allSales.isChecked = (salesType == SalesViewModel.Companion.SalesType.ALL_SALES)
        binding.sold.isChecked = (salesType == SalesViewModel.Companion.SalesType.SOLD)
        binding.returns.isChecked = (salesType == SalesViewModel.Companion.SalesType.RETURNS)

        // Load data based on current filter state
        val currentFilter = viewModel.currentFilter.value
        val dateToUse = if (currentFilter?.isFiltered == true) currentFilter.date else null

        viewModel.loadSalesData(salesType, dateToUse)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerFragment { day, month, year ->
            val formatted = String.format(Locale.ENGLISH, "%02d-%02d-%04d", day, month, year)

            val currentFilter = viewModel.currentFilter.value
            val currentType = currentFilter?.type ?: SalesViewModel.Companion.SalesType.ALL_SALES

            viewModel.loadSalesData(currentType, formatted)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun resetFilters() {
        binding.date.text = ""
        viewModel.resetFilters()
    }

    private fun updateFilterUI(filter: SalesViewModel.Companion.SalesFilter) {
        binding.date.text = if (filter.isFiltered) filter.date else ""

        // Update chip selection based on current type
        binding.allSales.isChecked = (filter.type == SalesViewModel.Companion.SalesType.ALL_SALES)
        binding.sold.isChecked = (filter.type == SalesViewModel.Companion.SalesType.SOLD)
        binding.returns.isChecked = (filter.type == SalesViewModel.Companion.SalesType.RETURNS)
    }

    private fun onItemClick(soldProduct: SoldProduct) {
        DialogBuilder.showAlertDialog(
            context = this,
            title = "Options",
            message = "What would you like to do?",
            positiveButton = "View Product",
            negativeButton = "View Bill",
            onConfirm = {
                navigateToProduct(soldProduct)
            },
            onCancel = {
                navigateToBill(soldProduct)
            }
        )
    }

    private fun navigateToProduct(soldProduct: SoldProduct) {
        when (val result = viewModel.validateProductNavigation(soldProduct)) {
            is SalesViewModel.ValidationResult.Success -> {
                val intent = Intent(this, ProductActivity::class.java).apply {
                    putExtra("productId", soldProduct.productId)
                }
                startActivity(intent)
            }
            is SalesViewModel.ValidationResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToBill(soldProduct: SoldProduct) {
        when (val result = viewModel.validateBillNavigation(soldProduct)) {
            is SalesViewModel.ValidationResult.Success -> {
                val intent = Intent(this, BillDetailsActivity::class.java).apply {
                    putExtra("saleId", soldProduct.saleId)
                }
                startActivity(intent)
            }
            is SalesViewModel.ValidationResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCurrentData()
    }
}