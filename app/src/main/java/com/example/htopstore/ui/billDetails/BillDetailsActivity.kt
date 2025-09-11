package com.example.htopstore.ui.billDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.databinding.ActivityBillDetailsBinding
import com.example.htopstore.util.DialogBuilder
import com.example.htopstore.util.adapters.BillDetailsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BillDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillDetailsBinding
    private lateinit var sellOp: SalesOpsWithDetails
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
        viewModel.getSellOp(id,onEmptyProducts = {
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
        binding.discount.text = "${sellOp.saleOp.discount} %"
        binding.afterDiscount.text = "${sellOp.saleOp.totalCash} $"
        binding.billId.text = "Bill ID: #${sellOp.saleOp.saleId}"
        binding.date.text = "Date: ${sellOp.saleOp.date}"
        binding.time.text = sellOp.saleOp.time

        val totalBefore =
            (sellOp.saleOp.totalCash * 100 / (100 - sellOp.saleOp.discount)).toInt()
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
