package com.example.htopstore.ui.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.product.ProductRepoImp
import com.example.htopstore.databinding.ActivityProductBinding
import com.example.htopstore.domain.useCase.CartHandler
import com.example.htopstore.domain.useCase.GetAdapterOfOptionsUseCase
import com.example.htopstore.util.CartHelper
import com.example.htopstore.util.DialogBuilder
import com.example.htopstore.util.NAE.ae
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private lateinit var product: Product
    private lateinit var productRepo: ProductRepoImp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setControllers()
        fetchProduct()
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
        // get the id from the intent
        val id = intent.getStringExtra("productId")
        // get the product of the database
        productRepo = ProductRepoImp(this)
        lifecycleScope.launch(Dispatchers.IO) {
            product = productRepo.getProductById(id!!)!!
            // set the product to the view
            withContext(Dispatchers.Main) {
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
                binding.typeTv.setAdapter(GetAdapterOfOptionsUseCase.getCategoriesAdapter(this@ProductActivity))
            }
        }
    }

    private fun updateProduct() {
        if (!allFieldsDone()) return
        // get the new values from the form
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
        lifecycleScope.launch(Dispatchers.IO) {
            productRepo.updateProduct(product)
            withContext(Dispatchers.Main) {
                finish()
            }

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
        lifecycleScope.launch(Dispatchers.IO) {
            productRepo.deleteProductById(product.id, product.productImage)
            withContext(Dispatchers.Main) {
                CartHelper.removeFromTheCartList(product.id)
                finish()
            }
        }
    }
    private fun allFieldsDone(): Boolean {
        if (binding.typeTv.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.productBrandET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a brand", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.buyingPriceET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a selling price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.countET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a count", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.countET.text.toString().toInt() <= 0) {
            Toast.makeText(this, "Invalid count", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.buyingPriceET.text.toString().toDouble() <= 0) {
            Toast.makeText(this, "Invalid buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().toDouble() <= 0) {
            Toast.makeText(this, "Invalid selling price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().toDouble() <=
            binding.buyingPriceET.text.toString().toDouble()
        ) {
            Toast.makeText(this, "Selling price must be > buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun ProductActivity.onAddToCart() {
        CartHelper.addToTheCartList(product = product)
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
    }
}