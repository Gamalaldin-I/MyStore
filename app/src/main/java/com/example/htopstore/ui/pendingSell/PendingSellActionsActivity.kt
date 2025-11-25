package com.example.htopstore.ui.pendingSell

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
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
        }
    }

    private fun observeData() {
        viewModel.actions.observe(this) { actions ->
            if (actions.isNullOrEmpty()) {
                animateToEmptyState()
            } else {
                showContentState()
                updateHeaderInfo(actions.size)
                adapter.submitList(actions)
            }
        }
    }

    private fun setupClickListeners() {
        binding.syncAllBtn.setOnClickListener {
            if (!isSyncing) {
                showSyncConfirmationDialog()
            }
        }

        binding.emptyStateBtn.setOnClickListener {
            finish()
        }
    }

    private fun showSyncConfirmationDialog() {
        val pendingCount = viewModel.actions.value?.size ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sync_all))
            .setMessage(getString(R.string.syncing_all))
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

        // Start smooth animations
        startIconRotation()

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


    private fun startIconRotation() {
        rotationAnimator?.cancel()
        rotationAnimator = ObjectAnimator.ofFloat(binding.headerIcon, View.ROTATION, 0f, 360f).apply {
            duration = 1200
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopIconRotation() {
        rotationAnimator?.cancel()
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
            // Smooth progress update
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
            // Complete the progress bar
            overallProgress.setProgressCompat(100, true)
            progressPercentage.text = "100%"
            overallProgressText.text = "Completed!"

            // Small celebration animation

        }

        // Delete approved actions
        viewModel.deleteAllApprovedActions()

        // Show result after animation
        binding.root.postDelayed({
            showSyncResultDialog(totalActions)
        }, 400)
    }

    private fun showSyncResultDialog(totalActions: Int) {
        // Collapse the progress section

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
                if (failedCount == 0) "Sync Complete"
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
            // Fade out content
            pendingSalesRecyclerView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    pendingSalesRecyclerView.visibility = View.GONE
                    loadingStateLayout.visibility = View.GONE

                    // Show empty state
                    emptyStateLayout.visibility = View.VISIBLE
                    emptyStateLayout.alpha = 0f
                    emptyStateLayout.translationY = 20f

                    emptyStateLayout.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                .start()
        }
    }

    private fun showLoadingState() {
        binding.apply {
            emptyStateLayout.visibility = View.GONE
            pendingSalesRecyclerView.visibility = View.GONE
            loadingStateLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rotationAnimator?.cancel()
        isSyncing = false
    }
}