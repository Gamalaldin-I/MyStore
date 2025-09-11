package com.example.htopstore.ui.bills

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.repo.bills.BillRepoImp
import com.example.htopstore.databinding.ActivityBillsBinding
import com.example.htopstore.ui.billDetails.BillDetailsActivity
import com.example.htopstore.util.DatePickerFragment
import com.example.htopstore.util.adapters.BillsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BillsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillsBinding
    private lateinit var billsRepo: BillRepoImp
    private lateinit var adapter: BillsAdapter

    private var sinceDate: LocalDate? = null
    private var toDate: LocalDate? = null
    private var chosenReq = 0

    companion object {
        const val ALL_BILLS_REQ = 0
        const val FILTERED_BY_DATE_BILLS_REQ = 1
        const val FILTERED_BY_DATE_RANGE_BILLS_REQ = 2
        const val FILTERED_BY_DATE_TILL_DATE_BILLS_REQ = 3

        private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
        private val dbFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billsRepo = BillRepoImp(this)
        controlDate()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        getBillsByRequestCode(chosenReq)
    }

    private fun setupRecyclerView() {
        adapter = BillsAdapter(emptyList()) { saleId ->
            navigateToBillDetails(saleId)
        }
        binding.recyclerView.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun controlDate() {
        setupDatePicker(binding.since, isSince = true)
        setupDatePicker(binding.to, isSince = false)

        binding.reset.setOnClickListener {
            binding.since.text = "Since"
            binding.to.text = "To"
            sinceDate = null
            toDate = null
            getBillsByRequestCode(ALL_BILLS_REQ)
        }
    }

    private fun setupDatePicker(view: android.widget.TextView, isSince: Boolean) {
        view.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->

                val pickedDate = LocalDate.of(year, month, day)
                if (isSince) {
                    if (toDate != null && pickedDate.isAfter(toDate)) {
                        Toast.makeText(this, "Since date must be before To date", Toast.LENGTH_SHORT).show()
                        return@DatePickerFragment
                    }
                    sinceDate = pickedDate
                    binding.since.text = pickedDate.format(displayFormatter)
                } else {
                    if (sinceDate != null && pickedDate.isBefore(sinceDate)) {
                        Toast.makeText(this, "To date must be after Since date", Toast.LENGTH_SHORT).show()
                        return@DatePickerFragment
                    }
                    toDate = pickedDate
                    binding.to.text = pickedDate.format(displayFormatter)
                }

                getBillsByRequestCode(getUseCaseCode())
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    private fun getUseCaseCode() = when {
        sinceDate == null && toDate == null -> ALL_BILLS_REQ
        sinceDate != null && toDate == null -> FILTERED_BY_DATE_BILLS_REQ
        sinceDate != null && toDate != null -> FILTERED_BY_DATE_RANGE_BILLS_REQ
        else -> FILTERED_BY_DATE_TILL_DATE_BILLS_REQ
    }

    private fun getFilteredByDay(date: LocalDate) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list = billsRepo.getBillsByDate(date.format(dbFormatter))
                withContext(Dispatchers.Main) { adapter.updateData(list);updateSum() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BillsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBillsByDateRange(since: LocalDate, to: LocalDate) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list = billsRepo.getBillsByDateRange(since.format(dbFormatter), to.format(dbFormatter))
                withContext(Dispatchers.Main) { adapter.updateData(list);updateSum() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BillsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getAllBillsTillDate(date: LocalDate) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list = billsRepo.getBillsTillDate(date.format(dbFormatter))
                withContext(Dispatchers.Main) { adapter.updateData(list) ; updateSum()}
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BillsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getAllBills() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list = billsRepo.getAllBills()
                withContext(Dispatchers.Main) { adapter.updateData(list);updateSum()}
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BillsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBillsByRequestCode(requestCode: Int) {
        when (requestCode) {
            ALL_BILLS_REQ -> getAllBills()
            FILTERED_BY_DATE_BILLS_REQ -> sinceDate?.let { getFilteredByDay(it) }
            FILTERED_BY_DATE_RANGE_BILLS_REQ -> if (sinceDate != null && toDate != null) {
                getBillsByDateRange(sinceDate!!, toDate!!)
            }
            FILTERED_BY_DATE_TILL_DATE_BILLS_REQ -> toDate?.let { getAllBillsTillDate(it) }
        }
        chosenReq = requestCode
    }
    private fun updateSum(){
        binding.total.text = adapter.getSumOfBills().toString()
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
