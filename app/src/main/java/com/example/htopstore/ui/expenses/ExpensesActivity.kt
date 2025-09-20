package com.example.htopstore.ui.expenses

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.model.Expense
import com.example.htopstore.databinding.ActivityExpensesBinding
import com.example.htopstore.util.adapters.ExpenseAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private val viewModel: ExpensesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupControllers()
        setupRecyclerView()
    }

    private fun setupControllers() {
        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.AddNewExpense.setOnClickListener {
            onAddExpenseClick()
        }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(arrayListOf(), ::onItemClick)
        binding.recyclerView.adapter = expenseAdapter
    }

    private fun setupObservers() {
        // Observe expenses list
        viewModel.expenses.observe(this) { expenses ->
            expenseAdapter.updateData(expenses as ArrayList<Expense>)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
           // binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.AddNewExpense.isEnabled = !isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        // Observe success messages
        viewModel.successMessage.observe(this) { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }

        // Observe total amount
        viewModel.totalAmount.observe(this) { total ->
           // updateTotalAmount(total)
        }
    }

    private fun onItemClick(expense: Expense) {
        DialogBuilder.showExpensesDetailsDialog(expense, this)
    }

    private fun onAddExpenseClick() {
        DialogBuilder.showAddExpenseDialog(this) { category, description, amount, paymentMethod ->
            DialogBuilder.hideAddExpenseDialog()
            viewModel.addExpense(category, description, amount, paymentMethod)
        }
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        DialogBuilder.showAlertDialog(
            context = this,
            title = "Delete Expense",
            message = "Are you sure you want to delete this expense?",
            positiveButton = "Delete",
            negativeButton = "Cancel",
            onConfirm = {
                viewModel.deleteExpense(expense)
            }, onCancel = {}
        )
    }



    override fun onResume() {
        super.onResume()
        viewModel.refreshExpenses()
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideAddExpenseDialog()
        DialogBuilder.hideExpenseDetailsDialog()
    }
}