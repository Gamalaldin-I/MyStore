package com.example.htopstore.ui.genCode

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.Product
import com.example.htopstore.databinding.ActivityQrcodeGenBinding
import com.example.htopstore.util.adapters.QrSelectAdapter
import com.example.htopstore.util.helper.CodePdfGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenCodeActivity : AppCompatActivity() {
    private var products = listOf<Product>()

    private lateinit var binding: ActivityQrcodeGenBinding
    private val adapter by lazy {
        QrSelectAdapter(::onSelectionChanged)
    }
    private val viewModel: GenCodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeGenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        setupSearchBar()
        observeData()
    }

    private fun setupUI() {
        // Optimize RecyclerView performance
        binding.recyclerView.apply {
            adapter = this@GenCodeActivity.adapter
            layoutManager = LinearLayoutManager(this@GenCodeActivity)
        }
    }



    private fun setupListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            radioGroup.setOnCheckedChangeListener { _, _ ->
                updateGenerateButtonText()
            }

            generateQrs.setOnClickListener {
                generateCodes()
            }

            printer.setOnClickListener {
                showMessage("Print feature coming soon")
            }

            selectAllBtn.setOnClickListener {
                adapter.selectAll()
            }

            clearSelectionBtn.setOnClickListener {
                adapter.clearSelection()
            }
        }
    }

    private fun observeData() {
        viewModel.products.observe(this) { liveProducts ->
            this.products = liveProducts
            if (products.isEmpty()) {
                showEmptyState()
            } else {
                adapter.submitProducts(products)
                updateEmptyState()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun generateCodes() {
        val selectedProducts = adapter.getSelectedProducts()

        if (selectedProducts.isEmpty()) {
            showMessage("Please select at least one product")
            return
        }

        // Show loading state
        binding.generateQrs.isEnabled = false
        binding.generateQrs.text = "Generating..."

        lifecycleScope.launch {
            try {
                val generator = CodePdfGenerator(selectedProducts, this@GenCodeActivity)

                if (binding.barcode.isChecked) {
                    generator.generateBarcodesPdf()
                } else {
                    generator.generateQrCodesPdf()
                }

                showMessage("PDF generated successfully")
            } catch (e: Exception) {
                showMessage("Failed to generate PDF: ${e.message}")
            } finally {
                binding.generateQrs.isEnabled = true
                updateGenerateButtonText()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onSelectionChanged(count: Int) {
        binding.apply {
            selectionCount.text = "$count selected"
            selectionCount.visibility = if (count > 0) View.VISIBLE else View.GONE

            val hasSelection = count > 0
            generateQrs.isEnabled = hasSelection
            printer.isEnabled = hasSelection
        }
    }
    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentSearchQuery = s?.toString()?.trim() ?: ""

                // Show/hide clear button
                binding.btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // Apply filters
                adapter.filter(currentSearchQuery)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.clearFocus()
        }
    }


    private fun updateGenerateButtonText() {
        binding.generateQrs.text = if (binding.barcode.isChecked) {
            "Barcodes"
        } else {
            "QRs"
        }
    }

    private fun updateEmptyState() {
        val isEmpty = adapter.itemCount == 0
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.onDestroy() // Clean up coroutines
    }
}

