package com.example.htopstore.ui.bills

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.repo.salesRepo.SalesRepoImp
import com.example.htopstore.databinding.ActivityBillsBinding
import com.example.htopstore.util.adapters.BillsAdapter
import com.example.htopstore.util.DatePickerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class BillsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillsBinding
    private lateinit var salesRepo: SalesRepoImp
    private lateinit var adapter: BillsAdapter
    private lateinit var date: String
    private var filtered = false
    private var req =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        salesRepo = SalesRepoImp(this)
        controlDate()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (
            filtered
        ){
            getFiltered(date)
        }else{
            loadSellOps()
        }
    }

    private fun setupRecyclerView() {
        adapter = BillsAdapter(emptyList()) { saleId ->
            navigateToBillDetails(saleId)
        }
        binding.recyclerView.adapter = adapter
    }
    private fun controlDate() {
        binding.selectDay.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                val formatted = String.format(Locale.ENGLISH,"%02d-%02d-%04d", day, month, year)
                date = formatted
                filtered = true
                binding.date.text = formatted
                getFiltered(date)
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.reset.setOnClickListener {
            binding.date.text = ""
            filtered = false
            date = ""
            loadSellOps()
        }
    }
    private fun getFiltered( date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val l = salesRepo.getSalesByDate(date)
            withContext(Dispatchers.Main) {
                adapter.updateData(l)
            }
        }

    }

    private fun navigateToBillDetails(saleId: String) {
        val intent = Intent(this, BillDetailsActivity::class.java).apply {
            putExtra("saleId", saleId)
        }
        startActivity(intent)
    }

    private fun loadSellOps() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sellOps = salesRepo.getAllSellOp()
            withContext(Dispatchers.Main) {
                adapter.updateData(sellOps)
            }
        }
    }
}
