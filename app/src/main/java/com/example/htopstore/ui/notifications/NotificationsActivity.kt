package com.example.htopstore.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.Notification
import com.example.domain.util.NotificationTimeUtils
import com.example.htopstore.databinding.ActivityNotificationsBinding
import com.example.htopstore.util.BaseActivity
import com.example.htopstore.util.adapters.NotificationsAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : BaseActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private val vm: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter
    private var allNotifications = listOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupAdapter()
        setupFilterChips()
        observeViewModel()
        getNotifications()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Notifications"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupAdapter() {
        adapter = NotificationsAdapter(
            onItemClick = { notification -> handleNotificationClick(notification) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = this@NotificationsActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                // Show all when no chip is selected
                adapter.submitList(allNotifications)
                updateEmptyState(allNotifications.isEmpty())
                return@setOnCheckedStateChangeListener
            }

            val filteredList = when (checkedIds.first()) {
                binding.chipToday.id -> filterByPeriod(FilterPeriod.TODAY)
                binding.chipYesterday.id -> filterByPeriod(FilterPeriod.YESTERDAY)
                binding.chipThisWeek.id -> filterByPeriod(FilterPeriod.THIS_WEEK)
                else -> allNotifications
            }

            adapter.submitList(filteredList)
            updateEmptyState(filteredList.isEmpty())
        }
    }

    private fun observeViewModel() {
        vm.notifications.observe(this) { notifications ->
            allNotifications = notifications.sortedByDescending { it.createdAt }

            // Apply current filter if any chip is selected
            val checkedChipId = binding.chipGroup.checkedChipId
            if (checkedChipId != View.NO_ID) {
                val filteredList = when (checkedChipId) {
                    binding.chipToday.id -> filterByPeriod(FilterPeriod.TODAY)
                    binding.chipYesterday.id -> filterByPeriod(FilterPeriod.YESTERDAY)
                    binding.chipThisWeek.id -> filterByPeriod(FilterPeriod.THIS_WEEK)
                    else -> allNotifications
                }
                adapter.submitList(filteredList)
                updateEmptyState(filteredList.isEmpty())
            } else {
                adapter.submitList(allNotifications)
                updateEmptyState(allNotifications.isEmpty())
            }

            showContent()
        }

        vm.message.observe(this) { message ->
            if (message.isNotEmpty() && allNotifications.isEmpty()) {
                showError(message)
            }
        }

        vm.isLoading.observe(this) { isLoading ->
            if (isLoading) showLoading() else showContent()
        }
    }

    private fun getNotifications() {
        showLoading()
        vm.getNotifications()
    }

    private enum class FilterPeriod {
        TODAY, YESTERDAY, THIS_WEEK
    }

    private fun filterByPeriod(period: FilterPeriod): List<Notification> {
        return allNotifications.filter { notification ->
            when (period) {
                FilterPeriod.TODAY -> NotificationTimeUtils.isToday(notification.createdAt)
                FilterPeriod.YESTERDAY -> NotificationTimeUtils.isYesterday(notification.createdAt)
                FilterPeriod.THIS_WEEK -> NotificationTimeUtils.isThisWeek(notification.createdAt)
            }
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        when {
            notification.billId.isNotEmpty() -> navigateToOrder(notification.billId)
            notification.productId.isNotEmpty() -> navigateToProduct(notification.productId)
            notification.storeId.isNotEmpty() -> navigateToStore(notification.storeId)
            else -> showNotificationDetails(notification)
        }
    }

    private fun navigateToOrder(orderId: String) {
        // TODO: Implement navigation to order details
        Snackbar.make(binding.root, "Opening order: $orderId", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToProduct(productId: String) {
        // TODO: Implement navigation to product details
        Snackbar.make(binding.root, "Opening product: $productId", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToStore(storeId: String) {
        // TODO: Implement navigation to store details
        Snackbar.make(binding.root, "Opening store: $storeId", Snackbar.LENGTH_SHORT).show()
    }

    private fun showNotificationDetails(notification: Notification) {
        Snackbar.make(binding.root, notification.description, Snackbar.LENGTH_LONG).show()
    }

    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showContent() {
        binding.loadingView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.loadingView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.errorMessage.text = message

        binding.retryButton.setOnClickListener {
            getNotifications()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}