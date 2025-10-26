package com.example.htopstore.ui.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.useCase.localize.NAE.digit
import com.example.domain.util.CartHelper
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityProductBinding
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

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

        setupToolbar()
        setControllers()
        observeMessages()
        fetchProduct()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle collapsing toolbar title
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / scrollRange.toFloat()

            // You can add additional animations here if needed
        })
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

        // Setup real-time profit calculation
        binding.buyingPriceET.addTextChangedListener {
            calculateProfit()
        }

        binding.sellingPriceET.addTextChangedListener {
            calculateProfit()
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
            // Load image with error handling
            Glide.with(this@ProductActivity)
                .load(File(product.productImage))
                .placeholder(R.drawable.fighter)
                .error(R.drawable.fighter)
                .centerCrop()
                .into(productImage)


            // Display product info in header
            type.text = product.category
            name.text = product.name
            quantityExist.text = product.count.toInt().ae()

            // Format sold count
            val soldCount = product.soldCount.toInt()
            wereSoldText.text = "${soldCount.ae()} sold"

            // Format date nicely
            addingDate.text = formatDate(product.addingDate)

            // Initialize form fields
            typeTv.setText(product.category)
            productBrandET.setText(product.name)
            buyingPriceET.setText(product.buyingPrice.toDouble().digit(1))
            sellingPriceET.setText(product.sellingPrice.toDouble().digit(1))
            countET.setText(product.count.toString())

            // Setup category dropdown
            typeTv.setAdapter(AutoCompleteHelper.getCategoriesAdapter(this@ProductActivity))

            // Calculate and display initial profit
            calculateProfit()

            // Update stock status badge color based on quantity
            updateStockStatus(product.count.toInt())
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val date = LocalDate.parse(dateString, inputFormatter)
            "Added: ${date.format(outputFormatter)}"
        } catch (e: Exception) {
            "Added: $dateString"
        }
    }

    private fun updateStockStatus(count: Int) {
        // You can add visual feedback based on stock level
        // This is optional and can be implemented based on your design
    }

    @SuppressLint("SetTextI18n")
    private fun calculateProfit() {
        val buyingPriceText = binding.buyingPriceET.text.toString()
        val sellingPriceText = binding.sellingPriceET.text.toString()

        val buyingPrice = buyingPriceText.toDoubleOrNull() ?: 0.0
        val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0

        val profit = sellingPrice - buyingPrice

        // Update profit display
        binding.profitPerItem.text = "${profit.digit(1)} LE"

        // Change color based on profit/loss
        val profitCard = binding.profitCard
        if (profit > 0) {
            profitCard.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.success_container)
            )
            binding.profitPerItem.setTextColor(
                ContextCompat.getColor(this, R.color.success)
            )
        } else if (profit < 0) {
            profitCard.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.error_container)
            )
            binding.profitPerItem.setTextColor(
                ContextCompat.getColor(this, R.color.error)
            )
        } else {
            profitCard.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.surface_variant)
            )
            binding.profitPerItem.setTextColor(
                ContextCompat.getColor(this, R.color.onSurfaceVariant)
            )
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

            // Validation
            if (type.isEmpty()) {
                typeLo.error = "Category is required"
                return
            }

            if (brand.isEmpty()) {
                productBrandLo.error = "Brand name is required"
                return
            }

            // Parse and validate numeric values
            val buyingPrice = buyingPriceString.toDoubleOrNull()
            val sellingPrice = sellingPriceString.toDoubleOrNull()
            val count = countString.toIntOrNull()

            if (buyingPrice == null || buyingPrice < 0) {
                buyingPriceLo.error = "Enter valid buying price"
                return
            }

            if (sellingPrice == null || sellingPrice < 0) {
                sellingPriceLo.error = "Enter valid selling price"
                return
            }

            if (count == null || count < 0) {
                countLo.error = "Enter valid quantity"
                return
            }

            // Warning if selling price is less than buying price
            if (sellingPrice < buyingPrice) {
                DialogBuilder.showAlertDialog(
                    context = this@ProductActivity,
                    message = "Selling price is lower than buying price. You will make a loss. Continue?",
                    title = "Warning",
                    positiveButton = "Continue",
                    negativeButton = "Cancel",
                    onConfirm = { saveProductChanges(currentProduct, type, brand, buyingPrice, sellingPrice, count) },
                    onCancel = {}
                )
                return
            }

            // Save changes
            saveProductChanges(currentProduct, type, brand, buyingPrice, sellingPrice, count)
        }
    }

    private fun saveProductChanges(
        currentProduct: Product,
        type: String,
        brand: String,
        buyingPrice: Double,
        sellingPrice: Double,
        count: Int
    ) {
        currentProduct.apply {
            category = type
            name = brand
            this.buyingPrice = buyingPrice
            this.sellingPrice = sellingPrice
            this.count = count
        }

        vm.updateProduct(currentProduct) {
            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showAlertBeforeDelete() {
        if (product == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        DialogBuilder.showAlertDialog(
            context = this,
            message = "Are you sure you want to delete this product? This action cannot be undone.",
            title = "Delete Product",
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
            Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun onAddToCart() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentProduct.count.toInt() <= 0) {
            Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show()
            return
        }

        CartHelper.addToTheCartList(product = currentProduct)
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
    }
}