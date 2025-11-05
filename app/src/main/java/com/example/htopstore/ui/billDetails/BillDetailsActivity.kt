package com.example.htopstore.ui.billDetails

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.domain.util.DateHelper
import com.example.htopstore.databinding.ActivityBillDetailsBinding
import com.example.htopstore.util.adapters.BillDetailsAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class BillDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillDetailsBinding
    private lateinit var sellOp: BillWithDetails
    private lateinit var adapter: BillDetailsAdapter
    private val viewModel: BillDetViewModel by viewModels()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupAdapter()
        setupClickListeners()
        getBill()
        setObservers()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Share button
        binding.shareButton.setOnClickListener {
            shareBill()
        }

        // Print button
        binding.printButton.setOnClickListener {
            printBill()
        }
    }

    private fun getBill() {
        val id = intent.getStringExtra("saleId")
        if (id.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid bill ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.getBill(id, onEmptyProducts = {
            Toast.makeText(this, "Bill has been deleted", Toast.LENGTH_SHORT).show()
            finish()
        })
    }

    private fun setupAdapter() {
        adapter = BillDetailsAdapter(mutableListOf()) { soldProduct ->
            onItemClick(soldProduct)
        }
        binding.recyclerView2.adapter = adapter
    }

    private fun setObservers() {
        viewModel.sellOp.observe(this) { billWithDetails ->
            sellOp = billWithDetails
            displayBillDetails()
        }

        viewModel.message.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayBillDetails() {
        try {
            // Bill ID
            binding.billId.text = "#${sellOp.bill.id}"

            // Date formatting
            binding.date.text = DateHelper.formatDate(sellOp.bill.date)

            // Time formatting
            binding.time.text = DateHelper.formatTime(sellOp.bill.time)

            // Calculate amounts
            val discount = sellOp.bill.discount
            val totalAfterDiscount = sellOp.bill.totalCash

            // Calculate total before discount
            val totalBefore = if (discount > 0) {
                (totalAfterDiscount * 100.0 / (100 - discount))
            } else {
                totalAfterDiscount.toDouble()
            }

            // Calculate discount amount
            val discountAmount = totalBefore - totalAfterDiscount

            // Display amounts with currency formatting
            binding.beforeDis.text = formatCurrency(totalBefore)
            binding.discount.text = "${discount}%"
            binding.discountAmount.text = "-${formatCurrency(discountAmount)}"
            binding.afterDiscount.text = formatCurrency(totalAfterDiscount.toDouble())

            // Update sold items
            if (sellOp.soldProducts.isNotEmpty()) {
                adapter.updateData(sellOp.soldProducts)
                binding.SaleDetails.text = "Sold Items (${sellOp.soldProducts.size})"
            } else {
                binding.SaleDetails.text = "No Items"
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying bill details", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }



    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    private fun onItemClick(soldProduct: SoldProduct) {
        DialogBuilder.showReturnDialog(this, soldProduct) { returnRequest ->
            viewModel.onClick(soldProduct, returnRequest) {
                Toast.makeText(
                    this,
                    "Bill deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun shareBill() {
        if (!::sellOp.isInitialized) {
            Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val billText = generateBillText()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Bill #${sellOp.bill.id}")
                putExtra(Intent.EXTRA_TEXT, billText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Bill Via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing bill", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun generateBillText(): String {
        val discount = sellOp.bill.discount
        val totalAfterDiscount = sellOp.bill.totalCash
        val totalBefore = if (discount > 0) {
            (totalAfterDiscount * 100.0 / (100 - discount))
        } else {
            totalAfterDiscount.toDouble()
        }

        return buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("        BILL RECEIPT")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("Bill ID: #${sellOp.bill.id}")
            appendLine("Date: ${binding.date.text}")
            appendLine("Time: ${binding.time.text}")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("ITEMS:")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            sellOp.soldProducts.forEach { item ->
                appendLine(item.name)
                appendLine("  Qty: ${item.quantity} × ${formatCurrency(item.sellingPrice.toDouble())}")
                appendLine("  Total: ${formatCurrency((item.quantity * item.sellingPrice).toDouble())}")
                appendLine()
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("Subtotal: ${formatCurrency(totalBefore)}")
            if (discount > 0) {
                appendLine("Discount: $discount% (-${formatCurrency(totalBefore - totalAfterDiscount)})")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("TOTAL: ${formatCurrency(totalAfterDiscount.toDouble())}")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("Thank you for your business!")
        }
    }

    private fun printBill() {
        if (!::sellOp.isInitialized) {
            Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(
            this,
            "Print feature will be implemented soon",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implement PDF generation and print functionality
        // Example:
        // try {
        //     val pdfGenerator = PdfGenerator(this)
        //     pdfGenerator.generateBillPdf(sellOp)
        // } catch (e: Exception) {
        //     Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
        // }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideReturnDialog()
    }
}