package com.example.htopstore.ui.expenses

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.model.Expense
import com.example.htopstore.data.local.repo.epenseRepo.ExpenseRepoImp
import com.example.htopstore.databinding.ActivityExpensesBinding
import com.example.htopstore.util.adapters.ExpenseAdapter
import com.example.htopstore.util.DateHelper
import com.example.htopstore.util.DialogBuilder
import com.example.htopstore.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityExpensesBinding
    private lateinit var expenseRepoImp: ExpenseRepoImp
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expensesList: ArrayList<Expense>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        expenseRepoImp = ExpenseRepoImp(this)
        setControllers()
        getTheExpenses()

    }
    private fun setControllers(){
        binding.backArrow.setOnClickListener {
            finish()
        }
        binding.AddNewExpense.setOnClickListener {
            onAddExpenseClick()
        }
        expenseAdapter = ExpenseAdapter(arrayListOf(), ::onItemClick)
        binding.recyclerView.adapter = expenseAdapter
    }
    private fun onItemClick(expense: Expense){
        DialogBuilder.showExpensesDetailsDialog(expense,this)
    }
    private fun onAddExpenseClick(){
        DialogBuilder.showAddExpenseDialog(this) { cat , des , amount , method ->
            // add to the database and update the recycler view
               val ex =Expense(
                    expenseId = IdGenerator.generateTimestampedId(),
                    date = DateHelper.getCurrentDate(),
                    time = DateHelper.getCurrentTime(),
                    description = des,
                    category = cat,
                    amount = amount,
                    paymentMethod = method)
            DialogBuilder.hideAddExpenseDialog()
            addExpenseToTheDatabase(ex)
            expenseAdapter.addExpense(ex)
        }
    }
    fun getTheExpenses(){
        lifecycleScope.launch(Dispatchers.IO){
           expensesList = expenseRepoImp.getAllExpenses() as  ArrayList
            if (expensesList.isNotEmpty()){
                withContext(Dispatchers.Main) {
                    expenseAdapter.updateData(expensesList)
                }
            }
        }
    }private fun addExpenseToTheDatabase(ex: Expense){
        lifecycleScope.launch(Dispatchers.IO){
            expenseRepoImp.insertExpense(ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideAddExpenseDialog()
        DialogBuilder.hideExpenseDetailsDialog()
    }
}