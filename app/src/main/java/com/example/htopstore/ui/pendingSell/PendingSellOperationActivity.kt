package com.example.htopstore.ui.pendingSell

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.PendingSellAction
import com.example.domain.util.Constants
import com.example.htopstore.databinding.ActivityPendingSellOperationBinding
import com.example.htopstore.util.adapters.PendingCartAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
@Suppress("DEPRECATION")
class PendingSellOperationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendingSellOperationBinding
    private lateinit var adapter: PendingCartAdapter
    private var pendingSellAction: PendingSellAction? = null
    private val vm: PendingSellActionViewModel by viewModels()
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingSellOperationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadPendingAction()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            if (!isProcessing) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupViews() {
        // Initially hide content for animation
        binding.apply {
            discountCard.alpha = 0f
            itemsCard.alpha = 0f
            summaryCard.alpha = 0f
            deleteBtn.alpha = 0f
            btnSyncNow.alpha = 0f

            // Set initial translations for slide-in effect
            discountCard.translationY = 50f
            itemsCard.translationY = 80f
            summaryCard.translationY = 110f
        }
    }

    private fun setupRecyclerView() {
        adapter = PendingCartAdapter(mutableListOf())
        binding.rvSoldItems.apply {
            layoutManager = LinearLayoutManager(this@PendingSellOperationActivity)
            adapter = this@PendingSellOperationActivity.adapter
            itemAnimator?.apply {
                addDuration = 300
                removeDuration = 300
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSyncNow.setOnClickListener {
                if (!isProcessing) {
                    showSyncConfirmation()
                }
            }

            deleteBtn.setOnClickListener {
                if (!isProcessing) {
                    showDeleteConfirmation()
                }
            }
        }
    }

    private fun loadPendingAction() {
        val id = intent.getIntExtra("PenId", -1)

        if (id == -1) {
            showError("Invalid pending action ID") {
                finish()
            }
            return
        }

        showLoadingState()

        vm.getPendingActionById(id = id) { action ->
                pendingSellAction = action
                populateViews(action)
                animateContentIn()
        }
    }

    private fun showLoadingState() {
        binding.apply {
            discountCard.isVisible = true
            itemsCard.isVisible = true
            summaryCard.isVisible = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateViews(action: PendingSellAction) {
        binding.apply {
            if(action.status == "Approved"){
                btnSyncNow.visibility = View.GONE
            }
            // Set discount
            TVDiscount.text = "${action.discount}%"

            // Set item count
            tvItemCount.text = action.soldProducts.size.toString()

            // Populate RecyclerView
            adapter.updateList(action.soldProducts)

            // Calculate totals
            val (subtotal,total) = adapter.getTotalOrderPriceAndTotalAfterDiscount(action.discount)

            // Animate price updates
            animatePrice(tvSubtotal, 0.0, subtotal)
            animatePrice(tvTotalPrice, 0.0, total)
        }
    }

    private fun animateContentIn() {
        val animatorSet = AnimatorSet()

        binding.apply {
            // Discount card animation
            val discountFade = ObjectAnimator.ofFloat(discountCard, View.ALPHA, 0f, 1f)
            val discountSlide = ObjectAnimator.ofFloat(discountCard, View.TRANSLATION_Y, 50f, 0f)

            // Items card animation
            val itemsFade = ObjectAnimator.ofFloat(itemsCard, View.ALPHA, 0f, 1f)
            val itemsSlide = ObjectAnimator.ofFloat(itemsCard, View.TRANSLATION_Y, 80f, 0f)

            // Summary card animation
            val summaryFade = ObjectAnimator.ofFloat(summaryCard, View.ALPHA, 0f, 1f)
            val summarySlide = ObjectAnimator.ofFloat(summaryCard, View.TRANSLATION_Y, 110f, 0f)

            // Button animations
            val deleteFade = ObjectAnimator.ofFloat(deleteBtn, View.ALPHA, 0f, 1f)
            val syncFade = ObjectAnimator.ofFloat(btnSyncNow, View.ALPHA, 0f, 1f)

            animatorSet.apply {
                // Play cards sequentially for better visual effect
                playSequentially(
                    AnimatorSet().apply {
                        playTogether(discountFade, discountSlide)
                        duration = 300
                    },
                    AnimatorSet().apply {
                        playTogether(itemsFade, itemsSlide)
                        duration = 300
                    },
                    AnimatorSet().apply {
                        playTogether(summaryFade, summarySlide, deleteFade, syncFade)
                        duration = 300
                    }
                )
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun animatePrice(
        textView: android.widget.TextView,
        fromValue: Double,
        toValue: Double,
        isNegative: Boolean = false
    ) {
        val animator = ValueAnimator.ofFloat(fromValue.toFloat(), toValue.toFloat())
        animator.apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                val formattedValue = formatter.format(value.toDouble())
                textView.text = if (isNegative) "-$formattedValue" else formattedValue
            }
            start()
        }
    }

    private fun showSyncConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sync Transaction")
            .setMessage("Are you sure you want to sync this pending sale? This action cannot be undone.")
            .setPositiveButton("Sync") { _, _ ->
                syncPendingAction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Pending Sale")
            .setMessage("Are you sure you want to delete this pending sale? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deletePendingAction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun syncPendingAction() {
        val action = pendingSellAction ?: return

        isProcessing = true
        showProcessingState(true)

        vm.syncPendingProcess(
            pendingSellAction = action,
            onProgress = { progress ->
                updateProgress(progress)
            },
            onFinish = { msg ->
                isProcessing = false
                if (msg == Constants.SELL_COMPLETED_MESSAGE) {
                    showSuccessAnimation {
                        finish()
                    }
                } else {
                    showProcessingState(false)
                    showError("Failed to sync transaction. Please try again.")
                }
            }
        )
    }

    private fun deletePendingAction() {
        val action = pendingSellAction ?: return

        isProcessing = true
        showProcessingState(true)

        vm.deletePendingActionById(
            id = action.id,
            onFinish = {
                isProcessing = false
                animateDeleteAndFinish()
            }
        )
    }

    private fun showProcessingState(processing: Boolean) {
        binding.apply {
            btnSyncNow.isEnabled = !processing
            deleteBtn.isEnabled = !processing
            toolbar.navigationIcon?.alpha = if (processing) 128 else 255

            btnSyncNow.text = if (processing) "Processing..." else "Sync Now"

            if (processing) {
                animateButtonProcessing(btnSyncNow)
            }
        }
    }

    private fun animateButtonProcessing(button: com.google.android.material.button.MaterialButton) {
        val scaleDown = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 0.97f)
        scaleDown.repeatCount = ValueAnimator.INFINITE
        scaleDown.repeatMode = ValueAnimator.REVERSE
        scaleDown.duration = 600
        scaleDown.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Float) {
        runOnUiThread {
            binding.btnSyncNow.text = "Syncing $progress%"
        }
    }

    private fun showSuccessAnimation(onComplete: () -> Unit) {
        binding.apply {
            // Scale animation for cards
            val discountScale = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(discountCard, View.SCALE_X, 1f, 1.05f, 1f),
                    ObjectAnimator.ofFloat(discountCard, View.SCALE_Y, 1f, 1.05f, 1f)
                )
            }

            val itemsScale = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(itemsCard, View.SCALE_X, 1f, 1.05f, 1f),
                    ObjectAnimator.ofFloat(itemsCard, View.SCALE_Y, 1f, 1.05f, 1f)
                )
            }

            val summaryScale = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(summaryCard, View.SCALE_X, 1f, 1.05f, 1f),
                    ObjectAnimator.ofFloat(summaryCard, View.SCALE_Y, 1f, 1.05f, 1f)
                )
            }

            val fadeOut = ObjectAnimator.ofFloat(scrollView, View.ALPHA, 1f, 0f)

            val successSet = AnimatorSet()
            successSet.apply {
                playSequentially(
                    AnimatorSet().apply {
                        playTogether(discountScale, itemsScale, summaryScale)
                        duration = 400
                    },
                    fadeOut.apply {
                        duration = 300
                    }
                )
                interpolator = OvershootInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onComplete()
                    }
                })
                start()
            }
        }
    }

    private fun animateDeleteAndFinish() {
        binding.apply {
            val slideOut = ObjectAnimator.ofFloat(scrollView, View.TRANSLATION_X, 0f, -1000f)
            val fadeOut = ObjectAnimator.ofFloat(scrollView, View.ALPHA, 1f, 0f)

            val deleteSet = AnimatorSet()
            deleteSet.apply {
                playTogether(slideOut, fadeOut)
                duration = 400
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        finish()
                    }
                })
                start()
            }
        }
    }

    private fun showError(message: String, onDismiss: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                onDismiss?.invoke()
            }
            .setCancelable(false)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isProcessing) {
            super.onBackPressed()
        }
    }
}