package com.example.htopstore.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.salesRepo.SalesRepoImp
import com.example.htopstore.databinding.ActivityReturnsBinding
import com.example.htopstore.ui.billDetails.BillDetailsActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.ReturnsAdapter
import com.example.htopstore.util.DatePickerFragment
import com.example.htopstore.util.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ReturnsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReturnsBinding
    private lateinit var salesRepo: SalesRepoImp
    private lateinit var adapter: ReturnsAdapter

    private var filtered = false
    private var date: String = ""
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
        adapter = ReturnsAdapter(ArrayList()) {
            onClick(it)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        controlChips()
        controlDate()

        binding.allSales.isChecked = true
        getData(selected)
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
    private fun onClick(soldProduct: SoldProduct){
        DialogBuilder.showAlertDialog(
            context = this,
            title ="Hello",
            message = "How can i help you",
            positiveButton = "View product",
            negativeButton = "View Bill",
            onConfirm = {
                if(soldProduct.productId == null){
                    Toast.makeText(this, "The product is not available now", Toast.LENGTH_SHORT).show()
                    return@showAlertDialog }
                val intent = Intent(this, ProductActivity::class.java)
                intent.putExtra("productId", soldProduct.productId)
                startActivity(intent)
            },
            onCancel = {
                if(soldProduct.saleId == null){
                    Toast.makeText(this, "The product has not been sold", Toast.LENGTH_SHORT).show()
                    return@showAlertDialog }
                val intent = Intent(this, BillDetailsActivity::class.java)
                intent.putExtra("saleId", soldProduct.saleId)
                startActivity(intent)
            }
        )
    }
}
