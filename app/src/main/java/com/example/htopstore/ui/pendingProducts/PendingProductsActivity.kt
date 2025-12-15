package com.example.htopstore.ui.pendingProducts

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.domain.model.Product
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityPendingProductsBinding
import com.example.htopstore.util.adapters.PendingProductsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendingProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingProductsBinding
    private lateinit var adapter: PendingProductsAdapter
    private val vm: PendingProductsViewModel by viewModels()

    // Animation objects
    private var iconRotationAnimator: ObjectAnimator? = null
    private var pulseAnimator: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeProducts()
        observeUploadState()
    }

    private fun setupUI() {
        // Setup toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup upload button
        binding.uploadBtn.setOnClickListener {
            animateButton(it)
            vm.uploadPendingProducts()
        }

        // Setup dismiss message button
        binding.dismissMessageBtn.setOnClickListener {
            animateButton(it)
            hideMessage()
        }
    }

    private fun observeProducts() {
        vm.products.observe(this) { products ->
            if (products.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                setupAdapter(products)
            }
        }
    }

    private fun observeUploadState() {
        vm.uploadState.observe(this) { state ->
            when (state) {
                is UploadState.Idle -> {
                    // Do nothing
                }
                is UploadState.Loading -> {
                    showLoading(state.progress, state.currentItem, state.totalItems)
                }
                is UploadState.Success -> {
                    hideLoading()
                    showSuccessMessage(
                        title = "Upload Complete!",
                        description = state.message,
                        details = "${state.itemCount} products uploaded successfully"
                    )
                }
                is UploadState.Error -> {
                    hideLoading()
                    showErrorMessage(
                        title = "Upload Failed",
                        description = state.message,
                        details = state.details
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupAdapter(list: List<Product>) {
        adapter = PendingProductsAdapter(list as ArrayList<Product>) { id, index ->
            adapter.onDelete(index)
            vm.deleteProductById(id) {
                // Optional: Show toast or snackbar
            }
        }
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    // ==================== Empty State ====================

    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.uploadBtn.isEnabled = false
        binding.uploadBtn.alpha = 0.5f
        animateEmptyState()
    }

    private fun hideEmptyState() {
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.uploadBtn.isEnabled = true
        binding.uploadBtn.alpha = 1f
    }

    // ==================== Loading State ====================

    private fun showLoading(progress: Int, currentItem: Int, totalItems: Int) {
        if (binding.loadingOverlay.visibility != View.VISIBLE) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.loadingOverlay.alpha = 0f
            binding.loadingOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .withStartAction {
                    startLoadingAnimation()
                }
                .start()
        }
        updateLoadingProgress(progress, currentItem, totalItems)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLoadingProgress(progress: Int, currentItem: Int, totalItems: Int) {
        val progressInt = progress.coerceIn(0, 100)

        // Update progress bar with animation
        binding.progressBar.setProgressCompat(progressInt, true)

        // Update percentage
        binding.progressPercentage.text = "$progressInt%"

        // Update progress text with contextual messages
        binding.progressText.text = when {
            progress < 25 -> "Preparing products..."
            progress < 50 -> "Uploading to server..."
            progress < 75 -> "Processing items..."
            progress < 95 -> "Finalizing upload..."
            else -> "Almost done!"
        }

        // Update item counter
        binding.itemCounter.text = "$currentItem of $totalItems products"
    }

    private fun hideLoading() {
        stopLoadingAnimation()
        binding.loadingOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.loadingOverlay.visibility = View.GONE
            }
            .start()
    }

    // ==================== Message Display ====================

    private fun showSuccessMessage(
        title: String,
        description: String,
        details: String? = null
    ) {
        binding.messageIconContainer.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.success)
        )
        binding.messageIcon.setImageResource(R.drawable.ic_check)
        showMessage(title, description, details)
    }

    private fun showErrorMessage(
        title: String,
        description: String,
        details: String? = null
    ) {
        binding.messageIconContainer.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.button_danger)
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

        animateMessageEntrance()
    }

    private fun hideMessage() {
        vm.resetUploadState()

        binding.messageCard.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .alpha(0f)
            .setDuration(250)
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
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        // Pulse animation for icon container
        val scaleX = ObjectAnimator.ofFloat(binding.loadingIcon, "scaleX", 1f, 1.15f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.loadingIcon, "scaleY", 1f, 1.15f, 1f)

        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 1200
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

    private fun animateMessageEntrance() {
        binding.messageCard.apply {
            scaleX = 0.7f
            scaleY = 0.7f
            alpha = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(450)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }

        binding.messageIconContainer.apply {
            scaleX = 0f
            scaleY = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(650)
                .setStartDelay(200)
                .setInterpolator(BounceInterpolator())
                .start()
        }
    }

    private fun animateEmptyState() {
        binding.emptyView.apply {
            alpha = 0f
            translationY = 50f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(OvershootInterpolator(1.5f))
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnimation()
    }
}