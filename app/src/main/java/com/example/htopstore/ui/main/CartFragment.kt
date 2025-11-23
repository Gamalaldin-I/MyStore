package com.example.htopstore.ui.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.CartHelper
import com.example.htopstore.databinding.FragmentCartBinding
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.util.CartHandler
import com.example.htopstore.util.adapters.CartRecycler
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.ceil

@AndroidEntryPoint
class CartFragment : Fragment() {

    companion object {
        private const val TAG = "CartFragment"
        private const val MAX_DISCOUNT = 50
        private const val DISCOUNT_INCREMENT = 1
    }

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartHandler: CartHandler
    private val viewModel: MainViewModel by activityViewModels()

    private var discount = 0
    private var discountValue = 0.0
    private var total = 0.0

    // Loading animation
    private var iconRotationAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupUI()
        setControllers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLoadingAnimation()
    }

    // ==================== UI Setup ====================

    private fun setupUI() {
        // Initial setup if needed
    }

    private fun refreshCart() {
        if (CartHelper.getAddedTOCartProducts().isEmpty()) {
            showEmptyState()
        } else {
            showCartContent()
        }
    }

    // ==================== Cart Content Management ====================

    private fun showCartContent() {
        cartHandler = CartHandler(CartHelper.getAddedTOCartProducts())
        total = cartHandler.getTheTotalCartPrice()

        // Reset discount
        discount = 0
        updatePrices(total, discount)

        // Setup RecyclerView
        setupRecyclerView()

        // Show cart UI
        binding.operationView.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = CartRecycler(
            cartHandler.getListOfCartProducts(),
            onDelete = { product ->
                handleProductDelete(product)
            },
            onIncOrDec = {
                handleQuantityChange()
            }
        )
    }

    private fun handleProductDelete(product: com.example.domain.model.CartProduct) {
        cartHandler.deleteFromTheCartList(product)
        CartHelper.removeFromTheCartList(product.id)

        // Recalculate
        total = cartHandler.getTheTotalCartPrice()
        updatePrices(total, discount)

        // Check if cart is empty
        if (cartHandler.getListOfCartProducts().isEmpty()) {
            showEmptyState()
        }
    }

    private fun handleQuantityChange() {
        total = cartHandler.getTheTotalCartPrice()
        updatePrices(total, discount)
    }

    private fun showEmptyState() {
        binding.operationView.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE

        // Reset values
        discount = 0
        total = 0.0
        discountValue = 0.0
    }

    // ==================== Controllers ====================

    private fun setControllers() {
        setupDiscountControls()
        setupActionButtons()
    }

    private fun setupDiscountControls() {
        binding.increaseBtn.setOnClickListener {
            increaseDiscount()
        }

        binding.decreaseBtn.setOnClickListener {
            decreaseDiscount()
        }
    }

    private fun setupActionButtons() {
        binding.sellNow.setOnClickListener {
            handleCompleteSale()
        }

        binding.clearCartBtn.setOnClickListener {
            handleClearCart()
        }

        binding.scanProductBtn.setOnClickListener {
            navigateToScan()
        }
    }

    // ==================== Discount Management ====================

    private fun increaseDiscount() {
        discount = (discount + DISCOUNT_INCREMENT).coerceAtMost(MAX_DISCOUNT)
        updatePrices(total, discount)
        animateButton(binding.increaseBtn)
    }

    private fun decreaseDiscount() {
        discount = (discount - DISCOUNT_INCREMENT).coerceAtLeast(0)
        updatePrices(total, discount)
        animateButton(binding.decreaseBtn)
    }

    private fun updatePrices(newPrice: Double, newDiscount: Int = 0) {
        total = newPrice
        discount = newDiscount
        discountValue = (total * discount) / 100.0

        // Update UI
        binding.totalValue.text = total.ae()
        binding.discountValue.text = discountValue.ae()
        binding.percentageView.text = discount.toString()
        binding.totalAfter.text = ceil(total - discountValue).toInt().ae()
    }

    // ==================== Sale Operations ====================

    private fun handleCompleteSale() {
        if (!::cartHandler.isInitialized || cartHandler.getListOfCartProducts().isEmpty()) {
            showToast("Cart is empty")
            return
        }

        showSaleConfirmationDialog()
    }

    private fun showSaleConfirmationDialog() {
        val totalAmount = ceil(total - discountValue).toInt()
        val itemCount = cartHandler.getListOfCartProducts().size

        DialogBuilder.showAlertDialog(
            context = requireContext(),
            title = "Complete Sale",
            message = "Complete sale of $itemCount item(s) for ${totalAmount.ae()} LE?",
            positiveButton = "Confirm",
            negativeButton = "Cancel",
            onConfirm = {
                processSale()
            },
            onCancel = { }
        )
    }

    private fun processSale() {
        val itemCount = cartHandler.getListOfCartProducts().size
        Log.d(TAG, "Processing sale: $itemCount items")

        showLoading(
            title = "Processing Sale",
            subtitle = "Updating inventory...",
            totalItems = itemCount
        )

        viewModel.sell(
            cartList = cartHandler.getListOfCartProducts(),
            discount = discount,
            onProgress = { progress ->
                updateLoadingProgress(progress, itemCount)
            }
        ) {
            hideLoading()
            onSaleComplete()
        }
    }

    private fun onSaleComplete() {
        showToast("Sale completed successfully!")
        clearCart()
    }

    // ==================== Loading State Management ====================

    private fun showLoading(
        title: String = "Processing",
        subtitle: String = "Please wait...",
        totalItems: Int = 0
    ) {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.loadingTitle.text = title
        binding.loadingSubtitle.text = subtitle

        // Reset progress
        binding.progressBar.progress = 0
        binding.progressPercentage.text = "0%"
        binding.progressText.text = "Starting..."

        if (totalItems > 0) {
            binding.itemCounter.visibility = View.VISIBLE
            binding.itemCounter.text = "0 of $totalItems items"
        } else {
            binding.itemCounter.visibility = View.GONE
        }

        // Start icon rotation animation
        startLoadingAnimation()

        // Disable interactions
        disableUserInteractions()
    }

    private fun updateLoadingProgress(progress: Float, totalItems: Int) {
        val progressInt = progress.toInt().coerceIn(0, 100)

        // Update progress bar with animation
        binding.progressBar.setProgressCompat(progressInt, true)

        // Update percentage
        binding.progressPercentage.text = "$progressInt%"

        // Update progress text
        val processedItems = ((progress / 100f) * totalItems).toInt()
        binding.progressText.text = when {
            progress < 30 -> "Processing items..."
            progress < 70 -> "Updating stock..."
            progress < 95 -> "Finalizing sale..."
            else -> "Almost done..."
        }

        // Update item counter
        binding.itemCounter.text = "$processedItems of $totalItems items"

        Log.d(TAG, "Progress: $progressInt% ($processedItems/$totalItems items)")
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
        stopLoadingAnimation()
        enableUserInteractions()
    }

    // ==================== Loading Animations ====================

    private fun startLoadingAnimation() {
        // Continuous rotation animation for the icon
        iconRotationAnimator = ObjectAnimator.ofFloat(
            binding.loadingIcon,
            "rotation",
            0f,
            360f
        ).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopLoadingAnimation() {
        iconRotationAnimator?.cancel()
        iconRotationAnimator = null
    }

    private fun disableUserInteractions() {
        binding.operationView.alpha = 0.5f
        binding.sellNow.isEnabled = false
        binding.increaseBtn.isClickable = false
        binding.decreaseBtn.isClickable = false
        binding.clearCartBtn.isClickable = false
    }

    private fun enableUserInteractions() {
        binding.operationView.alpha = 1f
        binding.sellNow.isEnabled = true
        binding.increaseBtn.isClickable = true
        binding.decreaseBtn.isClickable = true
        binding.clearCartBtn.isClickable = true
    }

    // ==================== Cart Management ====================

    private fun handleClearCart() {
        if (!::cartHandler.isInitialized || cartHandler.getListOfCartProducts().isEmpty()) {
            return
        }

        DialogBuilder.showAlertDialog(
            context = requireContext(),
            title = "Clear Cart",
            message = "Are you sure you want to clear all items from the cart?",
            positiveButton = "Clear",
            negativeButton = "Cancel",
            onConfirm = {
                clearCart()
                showToast("Cart cleared")
            },
            onCancel = { }
        )
    }

    private fun clearCart() {
        cartHandler.clearCartList()
        CartHelper.clearCartList()
        showEmptyState()
    }

    // ==================== Navigation ====================

    private fun navigateToScan() {
        showToast("Opening scanner...")
        startActivity(Intent(requireContext(), ScanActivity::class.java))
    }

    // ==================== UI Helpers ====================

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}