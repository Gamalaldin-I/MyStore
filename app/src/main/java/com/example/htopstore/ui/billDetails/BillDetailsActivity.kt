package com.example.htopstore.ui.billDetails

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.domain.model.User
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityBillDetailsBinding
import com.example.htopstore.util.adapters.BillDetailsAdapter
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var currentEmployee: User? = null
    private var billId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupAdapter()
        setupClickListeners()
        showLoading()
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
            if (::sellOp.isInitialized) {
                shareBill()
            } else {
                Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }

        // Print button
        binding.printButton.setOnClickListener {
            if (::sellOp.isInitialized) {
                printBill()
            } else {
                Toast.makeText(this, "Bill data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }

        // Contact employee button
        binding.contactEmployeeBtn.setOnClickListener {
            contactEmployee()
        }
    }

    private fun getBill() {
        billId = intent.getStringExtra("saleId")
        if (billId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid bill ID", Toast.LENGTH_SHORT).show()
            hideLoading()
            finish()
            return
        }

        viewModel.getBill(billId!!) {
            showDeleteConfirmationDialog {
                showLoading()
                viewModel.deleteBill(billId!!) {
                    Toast.makeText(
                        this,
                        "Bill deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter = BillDetailsAdapter(mutableListOf()) { soldProduct ->
            onItemClick(soldProduct)
        }
        binding.recyclerView2.adapter = adapter
    }

    private fun setObservers() {
        // Observe bill data
        viewModel.sellOp.observe(this) { billWithDetails ->
            if (billWithDetails != null) {
                sellOp = billWithDetails
                displayBillDetails()
            }
        }

        // Observe employee data
        viewModel.employee.observe(this) { employee ->
            if (employee != null) {
                currentEmployee = employee
                displayEmployeeInfo(employee)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        // Observe error messages
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                // If critical error, might want to finish activity
                if (error.contains("not found", ignoreCase = true)) {
                    finish()
                }
            }
        }

        // Observe general messages
        viewModel.message.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayEmployeeInfo(employee: User) {
        binding.apply {
            // Employee name
            employeeName.text = employee.name ?: "Unknown Employee"

            // Employee role/position
            employeeRole.text = when(employee.role){
                Constants.EMPLOYEE_ROLE -> "Employee"
                Constants.OWNER_ROLE -> "Owner"
                Constants.ADMIN_ROLE -> "Store Manager"
                Constants.CASHIER_ROLE ->"Sales Associate"
                else -> "Sales Associate"
            }

            // Load employee avatar
            Glide.with(this@BillDetailsActivity)
                .load(employee.photoUrl)
                .placeholder(R.drawable.icon_profile)
                .error(R.drawable.icon_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(employeeAvatar)

            // Show contact button only if employee has contact info
            contactEmployeeBtn.visibility = View.GONE


            // Show employee card
            employeeCard.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayBillDetails() {
        try {
            binding.apply {
                // Bill ID
                billId.text = "#${sellOp.bill.id}"

                // Date formatting
                date.text = DateHelper.formatDate(sellOp.bill.date)

                // Time formatting
                time.text = DateHelper.formatTime(sellOp.bill.time)

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
                beforeDis.text = formatCurrency(totalBefore)

                // Show/hide discount section
                if (discount > 0) {
                    discountLayout.visibility = View.VISIBLE
                    this.discount.text = "${discount}%"
                    binding.discountAmount.text = "-${formatCurrency(discountAmount)}"
                } else {
                    discountLayout.visibility = View.GONE
                }

                afterDiscount.text = formatCurrency(totalAfterDiscount.toDouble())

                // Update sold items
                if (sellOp.soldProducts.isNotEmpty()) {
                    adapter.updateData(sellOp.soldProducts)
                    SaleDetails.text = "Sold Items (${sellOp.soldProducts.size})"
                } else {
                    SaleDetails.text = "No Items"
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying bill details", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        // Disable action buttons during loading
        binding.shareButton.isEnabled = false
        binding.printButton.isEnabled = false
        binding.shareButton.alpha = 0.5f
        binding.printButton.alpha = 0.5f
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE

        // Enable action buttons
        binding.shareButton.isEnabled = true
        binding.printButton.isEnabled = true
        binding.shareButton.alpha = 1.0f
        binding.printButton.alpha = 1.0f
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    private fun onItemClick(soldProduct: SoldProduct) {
        DialogBuilder.showReturnDialog(this, soldProduct) { returnRequest ->
            showLoading()
            viewModel.onClick(soldProduct, returnRequest) {
                showDeleteConfirmationDialog {
                    showLoading()
                    viewModel.deleteBill(sellOp.bill.id) {
                        Toast.makeText(
                            this,
                            "Bill deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun contactEmployee() {
        currentEmployee?.let { employee ->
            val options = mutableListOf<String>()

            // Build options based on available contact info
            if (!"".isNullOrEmpty()) {
               // options.add("Call ${employee.name}")
                options.add("Send SMS")
            }
            if (!"".isNullOrEmpty()) {
                options.add("Send Email")
            }
            options.add("Cancel")

            MaterialAlertDialogBuilder(this)
                .setTitle("Contact ${employee.name}")
                .setItems(options.toTypedArray()) { dialog, which ->
                    when {
                        which < options.size - 1 -> handleContactOption(employee, options[which])
                        else -> dialog.dismiss()
                    }
                }
                .show()
        } ?: run {
            Toast.makeText(this, "Employee information not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleContactOption(employee: User, option: String) {
        when {
            option.startsWith("Call") -> callEmployee(employee)
            option.startsWith("Send SMS") -> sendMessage(employee)
            option.startsWith("Send Email") -> emailEmployee(employee)
        }
    }

    @SuppressLint("UseKtx")
    private fun sendMessage(employee: User) {
        "+201117559874"?.let { phone ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$phone")
                putExtra("sms_body", "Hi ${employee.name}, regarding Bill #${sellOp.bill.id}")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
    }

    private fun callEmployee(employee: User) {
        "+201117559874"?.let { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
    }

    private fun emailEmployee(employee: User) {
        employee.email?.let { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Regarding Bill #${sellOp.bill.id}")
                putExtra(Intent.EXTRA_TEXT, "Hi ${employee.name},\n\nI have a question about Bill #${sellOp.bill.id}.\n\n")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send Email"))
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to send email", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Email not available", Toast.LENGTH_SHORT).show()
    }

    private fun shareBill() {
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
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("         BILL RECEIPT")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("Bill ID: #${sellOp.bill.id}")
            appendLine("Date: ${binding.date.text}")
            appendLine("Time: ${binding.time.text}")
            currentEmployee?.let {
                appendLine("Served by: ${it.name}")
            }
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("ITEMS:")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            sellOp.soldProducts.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.name}")
                appendLine("   Qty: ${item.quantity} × ${formatCurrency(item.sellingPrice.toDouble())}")
                appendLine("   Subtotal: ${formatCurrency((item.quantity * item.sellingPrice).toDouble())}")
                appendLine()
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Subtotal: ${formatCurrency(totalBefore)}")
            if (discount > 0) {
                appendLine("Discount ($discount%): -${formatCurrency(totalBefore - totalAfterDiscount)}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("TOTAL: ${formatCurrency(totalAfterDiscount.toDouble())}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("Thank you for your business!")
            appendLine("We appreciate your patronage.")
        }
    }

    private fun printBill() {
        Toast.makeText(
            this,
            "Print feature will be implemented soon",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implement PDF generation and print functionality
        // try {
        //     val pdfGenerator = BillPdfGenerator(this)
        //     val pdfFile = pdfGenerator.generateBillPdf(sellOp, currentEmployee)
        //     // Open print dialog or share PDF
        // } catch (e: Exception) {
        //     Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        // }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Bill")
            .setMessage("Are you sure you want to delete this bill? This action cannot be undone.")
            .setIcon(R.drawable.ic_error_circle)
            .setPositiveButton("Delete") { _, _ ->
                onConfirm()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideReturnDialog()
    }
}