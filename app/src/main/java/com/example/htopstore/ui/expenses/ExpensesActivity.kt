package com.example.htopstore.ui.expenses

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityExpensesBinding
import com.example.htopstore.util.helper.Animator
import com.example.htopstore.util.helper.AutoCompleteHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding
    private val viewModel: ExpensesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setControllers()
        setupEnterAnimations()
    }

    private fun setupEnterAnimations() {
        // Animate cards sliding in on screen entry
        binding.scrollView.alpha = 0f
        binding.scrollView.translationY = 50f
        binding.scrollView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start()

        binding.confirmBtn.alpha = 0f
        binding.confirmBtn.translationY = 50f
        binding.confirmBtn.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(200)
            .start()
    }

    private fun setupObservers() {
        // Message observer (for errors and info messages)
        viewModel.message.observe(this) { message ->
            if (!message.isNullOrEmpty() && viewModel.isSuccess.value != true) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Shake the form on error
                if (message.contains("Please", ignoreCase = true) ||
                    message.contains("Error", ignoreCase = true)) {
                    shakeErrorFields()
                }
            }
        }

        // Loading observer
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        // Success observer
        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                showSuccessAnimation()
            }
        }
    }

    private fun setControllers() {
        // Back button with ripple effect
        binding.backButton.setOnClickListener {
            Animator.pulse(it, 0.95f, 150)
            it.postDelayed({ finish() }, 150)
        }

        // Setup dropdowns
        binding.categoryEt.setAdapter(AutoCompleteHelper.getExpenseCategoryAdapter(this))
        binding.methodET.setAdapter(AutoCompleteHelper.getPaymentMethodAdapter(this))

        // Confirm button with feedback animation
        binding.confirmBtn.setOnClickListener {
            // Button press animation
            Animator.pulse(it, 0.95f, 100)

            it.postDelayed({
                viewModel.validate(
                    binding.amountET.text.toString().trim(),
                    binding.categoryEt.text.toString().trim(),
                    binding.methodET.text.toString().trim(),
                    binding.desET.text.toString().trim()
                ) {
                    // Success callback handled by observer
                }
            }, 100)
        }
    }

    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.confirmBtn.isEnabled = false

        // Fade in with custom animation
        Animator.fadeIn(binding.loadingOverlay, 200)

        // Start rotating the progress indicator (if using custom drawable)
        // AnimationHelper.startLoadingRotation(binding.loadingProgress)
    }

    private fun hideLoading() {
        Animator.fadeOut(binding.loadingOverlay, 200, hideOnEnd = true) {
        }
    }

    private fun showSuccessAnimation() {
        binding.successOverlay.visibility = View.VISIBLE

        // Use the helper's success sequence
        Animator.successSequence(
            overlayView = binding.successOverlay,
            iconView = binding.successIconCard,
            duration = 500,
            holdDuration = 1500
        ) {
            // Animation complete, clear form
            clearFormWithAnimation()
            viewModel.resetSuccess()
        }
    }

    private fun shakeErrorFields() {
        // Shake the fields that might have errors
        when {
            binding.amountET.text.isNullOrEmpty() -> {
                Animator.shakeError(binding.amountLo)
            }
            binding.categoryEt.text.isNullOrEmpty() -> {
                Animator.shakeError(binding.categoryLo)
            }
            binding.methodET.text.isNullOrEmpty() -> {
                Animator.shakeError(binding.methodLo)
            }
        }
    }

    private fun clearFormWithAnimation() {
        // Fade out current values
        val views = listOf(
            binding.amountET,
            binding.categoryEt,
            binding.methodET,
            binding.desET
        )

        views.forEach { view ->
            view.animate()
                .alpha(0.3f)
                .setDuration(200)
                .withEndAction {
                    view.text?.clear()
                    view.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
        binding.confirmBtn.isEnabled = true


        // Smooth scroll to top
        binding.scrollView.smoothScrollTo(0, 0)

        // Focus on amount field with slight delay
        binding.root.postDelayed({
            binding.amountET.requestFocus()
        }, 400)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up all animations
        Animator.cancelAnimations(binding.loadingOverlay)
        Animator.cancelAnimations(binding.successOverlay)
        Animator.cancelAnimations(binding.successIconCard)
    }
}