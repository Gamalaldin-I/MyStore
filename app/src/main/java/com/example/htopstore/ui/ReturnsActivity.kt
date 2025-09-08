package com.example.htopstore.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.salesRepo.SalesRepoImp
import com.example.htopstore.databinding.ActivityReturnsBinding
import com.example.htopstore.util.adapters.ReturnsAdapter
import com.example.htopstore.util.DatePickerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ReturnsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReturnsBinding
    private lateinit var salesRepo: SalesRepoImp
    private lateinit var adapter: ReturnsAdapter

    private var filtered = false
    private var date: String = ""   // لازم يكون بنفس فورمات التخزين في الداتا
    private var selected = ALL_SALES

    companion object {
        private const val ALL_SALES = 0
        private const val SOLD = 1
        private const val RETURNS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReturnsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        salesRepo = SalesRepoImp(this)

        // RecyclerView setup
        adapter = ReturnsAdapter(ArrayList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        controlChips()
        controlDate()

        binding.allSales.isChecked = true
        getData(selected) // أول تحميل
    }

    private fun controlChips() {
        binding.allSales.setOnClickListener {
            selected = ALL_SALES
            if (filtered) getFiltered(selected, date) else getData(selected)
        }

        binding.sold.setOnClickListener {
            selected = SOLD
            if (filtered) getFiltered(selected, date) else getData(selected)
        }

        binding.returns.setOnClickListener {
            selected = RETURNS
            if (filtered) getFiltered(selected, date) else getData(selected)
        }
    }

    private fun controlDate() {
        binding.selectDay.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                val formatted = String.format(Locale.ENGLISH,"%02d-%02d-%04d", day, month, year)
                date = formatted
                filtered = true
                binding.date.text = formatted
                getFiltered(selected, date)
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.reset.setOnClickListener {
            binding.date.text = ""
            filtered = false
            date = ""
            getData(selected)
        }
    }

    private fun getFiltered(reqNum: Int, date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<SoldProduct> = when (reqNum) {
                ALL_SALES -> salesRepo.getAllSalesAndReturnsByDate(date)
                SOLD      -> salesRepo.getSoldOnlyByDate(date)
                RETURNS   -> salesRepo.getReturnsByDate(date)
                else      -> emptyList()
            }
            withContext(Dispatchers.Main) { updateData(list) }
        }
    }

    private fun getData(reqNum: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<SoldProduct> = when (reqNum) {
                ALL_SALES -> salesRepo.getAllSalesAndReturns()
                SOLD      -> salesRepo.getSoldOnly()
                RETURNS   -> salesRepo.getReturns()
                else      -> emptyList()
            }
            withContext(Dispatchers.Main) { updateData(list) }
        }
    }

    private fun updateData(newList: List<SoldProduct>) {
        adapter.updateData(newList)
       // binding.rowNum.text = adapter.itemCount.toString()
    }
}
