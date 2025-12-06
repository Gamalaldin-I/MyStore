package com.example.htopstore.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.Product
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.CartHelper
import com.example.domain.util.Constants
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentCartBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.util.CartHandler
import com.example.htopstore.util.adapters.CartRecycler
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.ceil

@AndroidEntryPoint
class CartFragment : Fragment() {

    companion object {
        private const val TAG = "CartFragment"
        private const val MAX_DISCOUNT = 50
        private const val DISCOUNT_INCREMENT = 1
        private const val MESSAGE_DISPLAY_DURATION = 7000L
    }

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartHandler: CartHandler
    private var cartList: List<Product> = emptyList()
    private val viewModel: MainViewModel by activityViewModels()

    private var discount = 0
    private var discountValue = 0.0
    private var total = 0.0

    // Animation objects
    private var iconRotationAnimator: ObjectAnimator? = null
    private var pulseAnimator: AnimatorSet? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupUI()
        setControllers()
        observeCart()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllAnimations()
        handler.removeCallbacksAndMessages(null)
    }

    // ==================== Cart Observation ====================

    private fun observeCart() {
        lifecycleScope.launch {
            CartHelper.cartList.collect { list ->
                cartList = list
                refreshCart()
            }
        }
    }

    // ==================== UI Setup ====================

    private fun setupUI() {
        // Animate summary card on creation
        animateSummaryCardEntrance()
    }

    private fun refreshCart() {
        if (cartList.isEmpty()) {
            showEmptyState()
        } else {
            showCartContent()
        }
    }

    // ==================== Cart Content Management ====================

    private fun showCartContent() {
        cartHandler = CartHandler(cartList as ArrayList)
        total = cartHandler.getTheTotalCartPrice()

        // Reset discount
        discount = 0
        updatePrices(total, discount)

        // Setup RecyclerView
        setupRecyclerView()

        // Show cart UI with animation
        binding.operationView.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        animateSummaryCardEntrance()
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

        // Recalculate with animation
        total = cartHandler.getTheTotalCartPrice()
        animatePriceUpdate(total, discount)

        // Check if cart is empty
        if (cartHandler.getListOfCartProducts().isEmpty()) {
            showEmptyState()
        }
    }

    private fun handleQuantityChange() {
        total = cartHandler.getTheTotalCartPrice()
        animatePriceUpdate(total, discount)
    }

    private fun showEmptyState() {
        binding.operationView.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE

        // Animate empty state entrance
        animateEmptyState()

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

        binding.dismissMessageBtn.setOnClickListener {
            hideMessage()
        }
    }

    // ==================== Discount Management ====================

    private fun increaseDiscount() {
        val oldDiscount = discount
        discount = (discount + DISCOUNT_INCREMENT).coerceAtMost(MAX_DISCOUNT)

        if (oldDiscount != discount) {
            animatePriceUpdate(total, discount)
            animateDiscountChange()
            animateButton(binding.increaseBtn)
        }
    }

    private fun decreaseDiscount() {
        val oldDiscount = discount
        discount = (discount - DISCOUNT_INCREMENT).coerceAtLeast(0)

        if (oldDiscount != discount) {
            animatePriceUpdate(total, discount)
            animateDiscountChange()
            animateButton(binding.decreaseBtn)
        }
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
        val totalAmount = ceil(total - discountValue).toInt()
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
            },
            onFiredAction ={
                onFiredAction()
            }
        ) {msg->
            hideLoading()
            if(msg == Constants.SELL_COMPLETED_MESSAGE){
            showSuccessMessage(
                title = "Sale Completed!",
                description = "Transaction successful",
                details = "$itemCount items sold for ${totalAmount.ae()} LE"
            )}
            else{
                showErrorMessage(
                    title = "Sale Pending!",
                    description = "Transaction failed",
                    details = msg
                )
            }
            clearCart()
        }
    }




    // ==================== Loading State Management ====================

    private fun showLoading(
        title: String = "Processing",
        subtitle: String = "Please wait...",
        totalItems: Int = 0
    ) {
        binding.loadingOverlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

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

        // Start animations
        startLoadingAnimation()
        disableUserInteractions()
    }

    @SuppressLint("SetTextI18n")
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
        binding.loadingOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.loadingOverlay.visibility = View.GONE
                stopLoadingAnimation()
                enableUserInteractions()
            }
            .start()
    }

    // ==================== Message Display ====================

    private fun showSuccessMessage(
        title: String,
        description: String,
        details: String? = null
    ) {
        // Configure success appearance
        binding.messageIconContainer.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.success)
        )
        binding.messageIcon.setImageResource(R.drawable.ic_check)

        showMessage(title, description, details)
    }

    private fun showErrorMessage(
        title: String,
        description: String,
        details: String? = null
    ) {
        // Configure error appearance
        binding.messageIconContainer.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.button_danger)
        )
        binding.messageIcon.setImageResource(R.drawable.ic_error_circle)

        showMessage(title, description, details)
    }

    private fun showMessage(
        title: String,
        description: String,
        details: String? = null
    ) {
        binding.messageTitle.text = title
        binding.messageDescription.text = description

        if (details != null) {
            binding.messageDetails.visibility = View.VISIBLE
            binding.messageDetails.text = details
        } else {
            binding.messageDetails.visibility = View.GONE
        }

        // Show overlay with animation
        binding.messageOverlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // Animate message card
        animateMessageEntrance()

        // Auto dismiss after duration
        handler.postDelayed({
            hideMessage()
        }, MESSAGE_DISPLAY_DURATION)
    }

    private fun hideMessage() {
        binding.messageCard.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.messageOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .setStartDelay(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.messageOverlay.visibility = View.GONE
            }
            .start()
    }

    // ==================== Animations ====================

    private fun startLoadingAnimation() {
        // Continuous rotation for icon
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

        // Pulse animation for icon container
        val scaleX = ObjectAnimator.ofFloat(binding.loadingIcon, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.loadingIcon, "scaleY", 1f, 1.1f, 1f)

        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (pulseAnimator != null) {
                        animation.start()
                    }
                }
            })
            start()
        }
    }

    private fun stopLoadingAnimation() {
        iconRotationAnimator?.cancel()
        iconRotationAnimator = null
        pulseAnimator?.cancel()
        pulseAnimator = null
    }

    private fun stopAllAnimations() {
        stopLoadingAnimation()
    }

    private fun animateMessageEntrance() {
        binding.messageCard.apply {
            scaleX = 0.7f
            scaleY = 0.7f
            alpha = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }

        // Animate icon with bounce
        binding.messageIconContainer.apply {
            scaleX = 0f
            scaleY = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(BounceInterpolator())
                .start()
        }
    }

    private fun animateSummaryCardEntrance() {
        binding.summaryCard.apply {
            translationY = 100f
            alpha = 0f

            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateEmptyState() {
        binding.emptyState.apply {
            alpha = 0f
            translationY = 50f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateDiscountChange() {
        //asign new discount value
        binding.percentageView.text = discount.toString()

        // Pulse animation for discount card
        binding.discountCard.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction {
                binding.discountCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        // Pulse percentage view
        binding.percentageView.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                binding.percentageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun animatePriceUpdate(newPrice: Double, newDiscount: Int) {
        val oldTotal = total

        // Calculate new values
        val newTotal = newPrice
        val newDiscountValue = ((newTotal * newDiscount) / 100.0).toInt()
        val newTotalAfter = ceil(newTotal - newDiscountValue).toInt()

        // Animate total value
        animateTextValue(
            binding.totalValue,
            oldTotal,
            newTotal
        )

        // Animate discount value
        binding.discountValue.text = newDiscountValue.ae()


        // Animate final total with scale
        binding.totalAfter.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(200)
            .withEndAction {
                binding.totalAfter.text = newTotalAfter.ae()
                binding.totalAfter.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()

        // Update internal values
        total = newTotal
        discount = newDiscount
        discountValue = newDiscountValue.toDouble()
    }

    private fun animateTextValue(textView: android.widget.TextView, from: Double, to: Double) {
        ValueAnimator.ofFloat(from.toFloat(), to.toFloat()).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                textView.text = value.toDouble().ae()
            }
            start()
        }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
            .start()
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

    // ==================== Helpers ====================

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun onFiredAction(){
    //end the activity and logout the user
    // go to the login
        viewModel.logout{
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()}
    }
}