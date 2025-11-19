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
import com.example.domain.util.DateHelper
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityProductBinding
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
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
            Toast.makeText(this,getString(R.string.invalid_product_id), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        vm.getProduct(productId)
        vm.product.observe(this) { fetchedProduct ->
            fetchedProduct?.let {
                product = it
                displayProduct(it)
            } ?: run {
                Toast.makeText(this,getString(R.string.product_data_not_available), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayProduct(product: Product) {
        with(binding) {
            // Load image with error handling
            Glide.with(this@ProductActivity)
                .load(product.productImage)
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .centerCrop()
                .into(productImage)


            // Display product info in header
            type.text = product.category
            name.text = product.name
            quantityExist.text = product.count.toInt().toString()

            // Format sold count
            val soldCount = product.soldCount.toInt()
            wereSoldText.text = "${soldCount.ae()} ${getString(R.string.sold)}"

            // Format date nicely
            addingDate.text = formatDate(product.addingDate)

            // Initialize form fields
            typeTv.setText(product.category)
            productBrandET.setText(product.name)
            buyingPriceET.setText(product.buyingPrice.toDouble().toString())
            sellingPriceET.setText(product.sellingPrice.toDouble().toString())
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
        binding.profitPerItem.text = "${profit.digit(1)} ${getString(R.string.le)}"

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
            Toast.makeText(this, getString(R.string.product_data_not_available), Toast.LENGTH_SHORT).show()
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
                typeLo.error = getString(R.string.category_required)
                return
            }

            if (brand.isEmpty()) {
                productBrandLo.error =getString(R.string.brand_required)
                return
            }

            // Parse and validate numeric values
            val buyingPrice = buyingPriceString.toDoubleOrNull()
            val sellingPrice = sellingPriceString.toDoubleOrNull()
            val count = countString.toIntOrNull()

            if (buyingPrice == null || buyingPrice < 0) {
                buyingPriceLo.error = getString(R.string.invalid_buying_price)
                return
            }

            if (sellingPrice == null || sellingPrice < 0) {
                sellingPriceLo.error = getString(R.string.invalid_selling_price)
                return
            }

            if (count == null || count < 0) {
                countLo.error = getString(R.string.invalid_count)
                return
            }

            // Warning if selling price is less than buying price
            if (sellingPrice < buyingPrice) {
                DialogBuilder.showAlertDialog(
                    context = this@ProductActivity,
                    message = getString(R.string.selling_price_must_be_greater),
                    title = getString(R.string.loss_warning_title),
                    positiveButton = getString(R.string.continue_button),
                    negativeButton = getString(R.string.cancel),
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
            lastUpdate = DateHelper.getTimeStampMilliSecond()
        }

        vm.updateProduct(currentProduct) {
            finish()
        }
    }

    private fun showAlertBeforeDelete() {
        if (product == null) {
            Toast.makeText(this, getString(R.string.product_data_not_available), Toast.LENGTH_SHORT).show()
            return
        }
        DialogBuilder.showAlertDialog(
            context = this,
            message = getString(R.string.delete_confirmation_message),
            title = getString(R.string.delete_confirmation_title),
            positiveButton = getString(R.string.delete),
            negativeButton = getString(R.string.cancel),
            onConfirm = { deleteProduct() },
            onCancel = {}
        )
    }

    private fun deleteProduct() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, getString(R.string.product_data_not_available), Toast.LENGTH_SHORT).show()
            return
        }

        vm.deleteProduct(currentProduct) {
            finish()
        }
    }

    private fun onAddToCart() {
        val currentProduct = product
        if (currentProduct == null) {
            Toast.makeText(this, getString(R.string.product_data_not_available), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentProduct.count.toInt() <= 0) {
            Toast.makeText(this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show()
            return
        }

        CartHelper.addToTheCartList(product = currentProduct)
        Toast.makeText(this,getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show()
    }
}