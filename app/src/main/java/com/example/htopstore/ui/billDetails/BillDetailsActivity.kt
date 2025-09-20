package com.example.htopstore.ui.billDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.htopstore.databinding.ActivityBillDetailsBinding
import com.example.htopstore.util.adapters.BillDetailsAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BillDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillDetailsBinding
    private lateinit var sellOp: BillWithDetails
    private lateinit var adapter: BillDetailsAdapter
    private val viewModel: BillDetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapter()
        getBill()
        setObservers()
    }

    private fun getBill(){
        val id = intent.getStringExtra("saleId")!!
        viewModel.getBill(id,onEmptyProducts = {
            Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show()
            finish()
        })
    }

    private fun setUpAdapter(){
        adapter = BillDetailsAdapter(mutableListOf()) {
            onClick(it)
        }
        binding.recyclerView2.adapter = adapter
    }

    private fun setObservers() {
        viewModel.sellOp.observe(this) {
            sellOp = it
            fetchSellOp()
        }

        viewModel.message.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchSellOp() {
        binding.discount.text = "${sellOp.bill.discount} %"
        binding.afterDiscount.text = "${sellOp.bill.totalCash} $"
        binding.billId.text = "Bill ID: #${sellOp.bill.saleId}"
        binding.date.text = "Date: ${sellOp.bill.date}"
        binding.time.text = sellOp.bill.time

        val totalBefore =
            (sellOp.bill.totalCash * 100 / (100 - sellOp.bill.discount)).toInt()
        binding.beforeDis.text = "$totalBefore $"

        if (sellOp.soldProducts.isNotEmpty()) {
            adapter.updateData(sellOp.soldProducts)
        }
    }

    private fun onClick(soldProduct: SoldProduct) {
        DialogBuilder.showReturnDialog(this, soldProduct) { returnRequest ->
            viewModel.onClick(soldProduct, returnRequest) {
                Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideReturnDialog()
    }
}
