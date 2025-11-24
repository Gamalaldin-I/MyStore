// ==================== FILE 1: PendingSellActionsActivity.kt ====================
package com.example.htopstore.ui.pendingSell

import android.os.Bundle
import android.view.View
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
                showEmptyState()
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

        binding.apply {
            syncAllBtn.isEnabled = false
            syncAllBtn.text = getString(R.string.syncing_all)
            overallProgress.visibility = View.VISIBLE
            overallProgress.max = 100
            overallProgress.progress = 0
            overallProgressText.visibility = View.VISIBLE
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

    private fun updateSyncProgress(
        progress: Float,
        currentIndex: Int,
        totalActions: Int,
        success: Boolean?
    ) {
        val overallProgress = ((currentIndex.toFloat() / totalActions) * 100).toInt()
        val itemProgress = progress.toInt()

        binding.apply {
            this.overallProgress.progress = overallProgress
            overallProgressText.text = "Syncing ${currentIndex + 1}/$totalActions ($itemProgress%)"
        }

        // Update individual item progress in the adapter
        viewModel.actions.value?.getOrNull(currentIndex)?.let { action ->
            adapter.updateProgress(action.id, itemProgress)
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

        binding.apply {
            overallProgress.visibility = View.GONE
            overallProgressText.visibility = View.GONE
            syncAllBtn.isEnabled = true
            syncAllBtn.text = getString(R.string.sync_all)
        }

        // Delete approved actions
        viewModel.deleteAllApprovedActions()

        // Build detailed result message
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

        // Show success message
        MaterialAlertDialogBuilder(this)
            .setTitle(
                if (failedCount == 0) "Sync Complete"
                else "Sync Partially Complete"
            )
            .setMessage(resultMessage)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // Check if there are still pending actions
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
            headerCard.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            loadingStateLayout.visibility = View.GONE
            pendingSalesRecyclerView.visibility = View.GONE
            headerCard.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
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
        isSyncing = false
    }
}