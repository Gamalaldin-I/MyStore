package com.example.htopstore.ui.expenses

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityExpensesBinding
import com.example.htopstore.util.helper.AutoCompleteHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding
    private val viewModel: ExpensesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setControllers()

        viewModel.message.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

    }
    private fun setControllers(){
        binding.backArrow.setOnClickListener {
            finish()
        }
        binding.categoryEt.setAdapter(AutoCompleteHelper.getExpenseCategoryAdapter(this))
        binding.methodET.setAdapter(AutoCompleteHelper.getPaymentMethodAdapter(this))

        binding.confirmBtn.setOnClickListener {
            viewModel.validate(
                binding.amountET.text.toString(),
                binding.categoryEt.text.toString(),
                binding.methodET.text.toString(),
                binding.desET.text.toString()
            ){
                binding.desET.text = null
                binding.amountET.text = null
                binding.categoryEt.text = null
                binding.methodET.text = null
                Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show()
            }
        }

    }


}