package com.example.htopstore.ui.pendingSell

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityPendingSellActionsBinding
import com.example.htopstore.util.adapters.SellPendingRecycler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendingSellActionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingSellActionsBinding
    private val viewModel: PendingSellActionViewModel by viewModels()
    private val adapter = SellPendingRecycler()

    private var isSyncing = false
    private var syncedCount = 0
    private var failedCount = 0
    private var rotationAnimator: ObjectAnimator? = null
    private var pulseAnimator: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingSellActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupRecyclerView()
        observeData()
        setupClickListeners()
        animateInitialLoad()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.pendingSalesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PendingSellActionsActivity)
            adapter = this@PendingSellActionsActivity.adapter
            setHasFixedSize(false)

            // Add item animation
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
            }
        }
    }

    private fun animateInitialLoad() {
        // Animate status card entrance
        binding.statusCard.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun observeData() {
        viewModel.actions.observe(this) { actions ->
            if (actions.isNullOrEmpty()) {
                animateToEmptyState()
            } else {
                showContentState()
                updateHeaderInfo(actions.size)
                adapter.submitList(actions) {
                    // Animate RecyclerView items
                    animateRecyclerViewEntrance()
                }
            }
        }
    }

    private fun animateRecyclerViewEntrance() {
        binding.pendingSalesRecyclerView.apply {
            if (alpha == 0f) {
                alpha = 0f
                translationY = 20f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun setupClickListeners() {
        binding.syncAllBtn.setOnClickListener {
            if (!isSyncing) {
                // Button press animation
                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()

                showSyncConfirmationDialog()
            }
        }

        binding.emptyStateBtn.setOnClickListener {
            // Button press animation
            finish()
        }
    }

    private fun showSyncConfirmationDialog() {
        val pendingCount = viewModel.actions.value?.size ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sync_all))
            .setMessage("Are you sure you want to sync all $pendingCount pending sales?" +
                    "Check you have good internet connection before proceeding.")
            .setPositiveButton(getString(R.string.sync_all)) { _, _ ->
                startSyncAll()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun startSyncAll() {
        isSyncing = true
        syncedCount = 0
        failedCount = 0
        val totalActions = viewModel.actions.value?.size ?: 0

        if (totalActions == 0) return

        // Show progress section with animation
        showProgressSection()

        // Start animations
        startIconRotation()
        startPulseEffect()

        binding.apply {
            syncAllBtn.isEnabled = false
            syncAllBtn.text = getString(R.string.syncing_all)
            overallProgress.progress = 0
        }

        viewModel.syncAllPendingActions(
            onFinish = {
                runOnUiThread {
                    handleSyncComplete(totalActions)
                }
            },
            onProgress = { progress, currentIndex, success ->
                runOnUiThread {
                    updateSyncProgress(progress, currentIndex, totalActions, success)
                }
            }
        )
    }

    private fun showProgressSection() {
        binding.progressSection.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = -20f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun hideProgressSection() {
        binding.progressSection.animate()
            .alpha(0f)
            .translationY(-20f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.progressSection.visibility = View.GONE
            }
            .start()
    }

    private fun startIconRotation() {
        rotationAnimator?.cancel()
        rotationAnimator = ObjectAnimator.ofFloat(binding.headerIcon, View.ROTATION, 0f, 360f).apply {
            duration = 1200
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun startPulseEffect() {
        binding.pulseEffect.visibility = View.VISIBLE

        val scaleX = ObjectAnimator.ofFloat(binding.pulseEffect, View.SCALE_X, 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        val scaleY = ObjectAnimator.ofFloat(binding.pulseEffect, View.SCALE_Y, 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        val alpha = ObjectAnimator.ofFloat(binding.pulseEffect, View.ALPHA, 0f, 0.3f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
    }

    private fun stopIconRotation() {
        rotationAnimator?.cancel()
        pulseAnimator?.cancel()

        binding.pulseEffect.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.pulseEffect.visibility = View.GONE
            }
            .start()

        binding.headerIcon.animate()
            .rotation(0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun updateSyncProgress(
        progress: Float,
        currentIndex: Int,
        totalActions: Int,
        success: Boolean?
    ) {
        val overallProgress = ((currentIndex.toFloat() / totalActions) * 100).toInt()

        binding.apply {
            // Smooth progress update with animation
            this.overallProgress.setProgressCompat(overallProgress, true)

            overallProgressText.text = "Syncing ${currentIndex + 1} of $totalActions..."
            progressPercentage.text = "$overallProgress%"
        }

        // Update individual item progress in adapter
        viewModel.actions.value?.getOrNull(currentIndex)?.let { action ->
            adapter.updateProgress(action.id, progress.toInt())
        }

        // Track success/failure when item completes
        if (success != null && progress >= 100f) {
            if (success) {
                syncedCount++
            } else {
                failedCount++
            }
        }
    }

    private fun handleSyncComplete(totalActions: Int) {
        isSyncing = false
        stopIconRotation()

        binding.apply {
            // Complete the progress bar with animation
            overallProgress.setProgressCompat(100, true)
            progressPercentage.text = "100%"
            overallProgressText.text = "Completed!"

            // Success animation - scale bounce
            iconContainer.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(200)
                .setInterpolator(OvershootInterpolator())
                .withEndAction {
                    iconContainer.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(BounceInterpolator())
                        .start()
                }
                .start()
        }

        // Delete approved actions
        viewModel.deleteAllApprovedActions()

        // Show result after animation
        binding.root.postDelayed({
            showSyncResultDialog(totalActions)
            hideProgressSection()
        }, 800)
    }

    private fun showSyncResultDialog(totalActions: Int) {
        val resultMessage = buildString {
            if (failedCount == 0) {
                append(getString(R.string.all_items_synced_successfully))
            } else {
                append("Sync Results:\n\n")
                append("✓ Successful: $syncedCount\n")
                append("✗ Failed: $failedCount\n")
                append("\nTotal: $totalActions")
            }
        }

        // Re-enable button
        binding.apply {
            syncAllBtn.isEnabled = true
            syncAllBtn.text = getString(R.string.sync_all)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(
                if (failedCount == 0) "Sync Complete ✓"
                else "Sync Partially Complete"
            )
            .setMessage(resultMessage)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                if (viewModel.actions.value.isNullOrEmpty()) {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun updateHeaderInfo(count: Int) {
        binding.apply {
            pendingCountText.text = resources.getQuantityString(
                R.plurals.pending_sales_count,
                count,
                count
            )
            pendingSubtitle.text = if (count > 0) {
                getString(R.string.sync_required)
            } else {
                getString(R.string.all_synced)
            }
        }
    }

    private fun showContentState() {
        binding.apply {
            loadingStateLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            pendingSalesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun animateToEmptyState() {
        binding.apply {
            // Hide status card with fade out
            statusCard.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    statusCard.visibility = View.GONE
                }
                .start()

            // Fade out content
            pendingSalesRecyclerView.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(250)
                .withEndAction {
                    pendingSalesRecyclerView.visibility = View.GONE
                    loadingStateLayout.visibility = View.GONE

                    // Show empty state with beautiful animation
                    emptyStateLayout.visibility = View.VISIBLE
                    emptyStateLayout.alpha = 0f
                    emptyStateLayout.translationY = 40f

                    emptyStateLayout.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setInterpolator(OvershootInterpolator(0.8f))
                        .start()

                    // Animate empty state elements
                    animateEmptyStateElements()
                }
                .start()
        }
    }

    private fun animateEmptyStateElements() {
        binding.apply {
            // Animate icon with scale and rotation
            emptyStateImage.apply {
                scaleX = 0f
                scaleY = 0f
                rotation = -180f
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0f)
                    .setDuration(600)
                    .setInterpolator(OvershootInterpolator(1.2f))
                    .setStartDelay(200)
                    .start()
            }

            // Animate title
            emptyStateTitle.apply {
                alpha = 0f
                translationY = 20f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(DecelerateInterpolator())
                    .setStartDelay(400)
                    .start()
            }

            // Animate message
            emptyStateMessage.apply {
                alpha = 0f
                translationY = 20f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(DecelerateInterpolator())
                    .setStartDelay(500)
                    .start()
            }

            // Animate button
            emptyStateBtn.apply {
                alpha = 0f
                translationY = 20f
                scaleX = 0.8f
                scaleY = 0.8f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(OvershootInterpolator(0.9f))
                    .setStartDelay(600)
                    .start()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        rotationAnimator?.cancel()
        pulseAnimator?.cancel()
        isSyncing = false
    }
}