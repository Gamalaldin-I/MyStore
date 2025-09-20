package com.example.htopstore.ui.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.CartHelper
import com.example.htopstore.databinding.ActivityProductBinding
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
@AndroidEntryPoint
class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private lateinit var product: Product
    private val vm: ProductViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setControllers()
        opsMsg()
        fetchProduct()
    }
    private fun opsMsg(){
        vm.message.observe(this){
            Toast.makeText(this, it , Toast.LENGTH_SHORT).show()
        }
    }


    private fun setControllers() {
        binding.saveChanging.setOnClickListener {
            updateProduct()
        }
        binding.backArrow.setOnClickListener {
            finish()
        }
        binding.delete.setOnClickListener {
            showAlertBeforeDelete()
        }
        binding.addToCart.setOnClickListener {
            onAddToCart()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchProduct() {
        val id = intent.getStringExtra("productId")
        vm.getProduct(id ?:"")
        vm.product.observe(this){  product->
            this.product = product!!
        Glide.with(binding.productImage)
            .load(File(product.productImage))
            .into(binding.productImage)
                binding.type.text = product.category
                binding.name.text = product.name
                binding.quantityExist.text = product.count.toInt().ae()
                binding.wereSoldText.text = product.soldCount.ae() + " were sold"
                binding.addingDate.text = "Adding date " + product.addingDate
                // initialize the form
                binding.typeTv.setText(product.category)
                binding.productBrandET.setText(product.name)
                binding.buyingPriceET.setText(product.buyingPrice.toInt().ae())
                binding.sellingPriceET.setText(product.sellingPrice.toInt().ae())
                binding.countET.setText(product.count.ae())
                binding.typeTv.setAdapter(AutoCompleteHelper.getCategoriesAdapter(this@ProductActivity))
            }
    }


    private fun updateProduct() {
        val type = binding.typeTv.text.toString()
        val brand = binding.productBrandET.text.toString()
        val buyingPrice = binding.buyingPriceET.text.toString().toInt()
        val sellingPrice = binding.sellingPriceET.text.toString().toInt()
        val count = binding.countET.text.toString().toInt()
        // update the product
        product.category = type
        product.name = brand
        product.buyingPrice = buyingPrice.toDouble()
        product.sellingPrice = sellingPrice.toDouble()
        product.count = count
        vm.updateProduct(product){
            finish()
        }
    }

    private fun showAlertBeforeDelete() {
        DialogBuilder.showAlertDialog(
            context = this,
            message = "Are you sure you want to delete this product?",
            title = "Delete",
            positiveButton = "Delete", negativeButton = "Undo", onConfirm = {
                deleteProduct()
            }, onCancel = {})
    }

    private fun deleteProduct() {
        vm.deleteProduct(product){
            finish()
    }
    }


    private fun ProductActivity.onAddToCart() {
        CartHelper.addToTheCartList(product = product)
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
    }
}
