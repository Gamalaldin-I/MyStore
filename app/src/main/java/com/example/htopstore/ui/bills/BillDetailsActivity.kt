package com.example.htopstore.ui.bills

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.data.local.repo.salesRepo.SalesRepoImp
import com.example.htopstore.databinding.ActivityBillDetailsBinding
import com.example.htopstore.util.adapters.BillDetailsAdapter
import com.example.htopstore.util.DialogBuilder
import com.example.htopstore.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class BillDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillDetailsBinding
    private lateinit var sellOp : SalesOpsWithDetails
    private lateinit var salesRepo: SalesRepoImp
    private lateinit var productRepo: ProductRepoImp
    private lateinit var adapter: BillDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBillDetailsBinding.inflate(layoutInflater)
        salesRepo = SalesRepoImp(this)
        productRepo = ProductRepoImp(this)
        val id = intent.getStringExtra("saleId")!!
        getSellOp(id)
        setContentView(binding.root)
        adapter = BillDetailsAdapter(mutableListOf()){
            onClick(it)
        }
        binding.recyclerView2.adapter = adapter


    }
    @SuppressLint("SetTextI18n", "DefaultLocale")
    fun getSellOp(id:String){
        lifecycleScope.launch(Dispatchers.IO) {
            sellOp = salesRepo.getSaleWithDetails(id)!!
            if(true){
            withContext (Dispatchers.Main) {
                binding.discount.text = "${sellOp.saleOp.discount} %"
                binding.afterDiscount.text = "${sellOp.saleOp.totalCash} $"
                binding.billId.text = "Bill ID: #${sellOp.saleOp.saleId}"
                binding.date.text = "Date: ${sellOp.saleOp.date}"
                binding.time.text = sellOp.saleOp.time
                val totalBefore = (sellOp.saleOp.totalCash * 100 / (100 - sellOp.saleOp.discount)).toInt()
                binding.beforeDis.text = "$totalBefore $"
                if(sellOp.soldProducts.isNotEmpty()){
                    adapter.updateData(sellOp.soldProducts)
                }
            }
            }
    }



}
    fun onClick(soldProduct: SoldProduct){
            DialogBuilder.showReturnDialog(this,soldProduct) { returnedProduct ->
                lifecycleScope.launch(Dispatchers.IO) {
                    salesRepo.updateSaleCashAfterReturn(
                        soldProduct.saleId!!,
                        returnedProduct.sellingPrice * abs(returnedProduct.quantity)
                    )
                    // insert returned product
                    salesRepo.insertSoldProduct(returnedProduct)

                    // update product quantity
                    productRepo.updateProductQuantity(returnedProduct.productId!!, returnedProduct.quantity)
                    val old = soldProduct.copy()
                    old.quantity = -returnedProduct.quantity
                    old.detailId = IdGenerator.generateTimestampedId()
                    old.saleId = null
                    salesRepo.insertSoldProduct(old)


                    // update the sold product quantity
                    val updatedSold = soldProduct.copy()
                    updatedSold.quantity -= -returnedProduct.quantity

                    if (updatedSold.quantity <= 0) {
                        salesRepo.deleteSoldProduct(updatedSold)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@BillDetailsActivity, "Item removed from bill", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        salesRepo.updateSoldProduct(updatedSold)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@BillDetailsActivity, "Item updated in bill", Toast.LENGTH_SHORT).show()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        getSellOp(sellOp.saleOp.saleId)
                    }
                }
            }


    }

    fun deleteSale(id:String){
        lifecycleScope.launch(Dispatchers.IO) {
            salesRepo.deleteSaleById(id)
            with(Dispatchers.Main) {
                finish()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        DialogBuilder.hideReturnDialog()
    }
}