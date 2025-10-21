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
    private var product: Product? = null
    private val vm: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setControllers()
        observeMessages()
        fetchProduct()
    }

    private fun observeMessages() {
        vm.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
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
        val productId = intent.getStringExtra("productId")

        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        vm.getProduct(productId)
        vm.product.observe(this) { fetchedProduct ->
            fetchedProduct?.let {
                product = it
                displayProduct(it)
            } ?: run {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayProduct(product: Product) {
        with(binding) {
            // Load image safely
            Glide.with(productImage)
                .load(File(product.productImage))
                .into(productImage)

            // Display product info
            type.text = product.category
            name.text = product.name
            quantityExist.text = product.count.toInt().ae()
            wereSoldText.text = "${product.soldCount.ae()} were sold"
            addingDate.text = "Adding date ${product.addingDate}"

            // Initialize form fields
            typeTv.setText(product.category)
            productBrandET.setText(product.name)
            buyingPriceET.setText(product.buyingPrice.toInt().ae())
            sellingPriceET.setText(product.sellingPrice.toInt().ae())
            countET.setText(product.count.ae())
            typeTv.setAdapter(AutoCompleteHelper.getCategoriesAdapter(this@ProductActivity))
        }
    }

    private fun updateProduct() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        with(binding) {
            val type = typeTv.text.toString().trim()
            val brand = productBrandET.text.toString().trim()
            val buyingPriceString = buyingPriceET.text.toString().trim()
            val sellingPriceString = sellingPriceET.text.toString().trim()
            val countString = countET.text.toString().trim()

            // Validate and parse numeric values
            val buyingPrice = buyingPriceString.toDoubleOrNull()
            val sellingPrice = sellingPriceString.toDoubleOrNull()
            val count = countString.toIntOrNull()

            if (buyingPrice == null || sellingPrice == null || count == null) {
                Toast.makeText(
                    this@ProductActivity,
                    "Please enter valid numeric values",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Update product with validated data
            currentProduct.apply {
                category = type
                name = brand
                this.buyingPrice = buyingPrice
                this.sellingPrice = sellingPrice
                this.count = count
            }

            vm.updateProduct(currentProduct) {
                finish()
            }
        }
    }

    private fun showAlertBeforeDelete() {
        if (product == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        DialogBuilder.showAlertDialog(
            context = this,
            message = "Are you sure you want to delete this product?",
            title = "Delete",
            positiveButton = "Delete",
            negativeButton = "Cancel",
            onConfirm = { deleteProduct() },
            onCancel = {}
        )
    }

    private fun deleteProduct() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        vm.deleteProduct(currentProduct) {
            finish()
        }
    }

    private fun onAddToCart() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        CartHelper.addToTheCartList(product = currentProduct)
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
    }
}