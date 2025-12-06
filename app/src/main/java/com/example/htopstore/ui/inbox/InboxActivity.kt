package com.example.htopstore.ui.inbox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.domain.model.remoteModels.Invitation
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityInboxBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.util.BaseActivity
import com.example.htopstore.util.adapters.InboxAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxActivity : BaseActivity() {

    private lateinit var binding: ActivityInboxBinding
    private lateinit var adapter: InboxAdapter
    private val viewModel: InboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupAdapter()
        setupClickListeners()
        observeViewModel()
        checkUserAuthentication()
    }

    private fun setupUI() {
        binding.apply {
            myEmail.text = viewModel.getEmail()
            updateEmptyState(show = true)
            setupSwipeRefresh()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.action_primary,
                R.color.background_dark,
                R.color.action_primary
            )

            // Configure swipe refresh behavior
            setProgressBackgroundColorSchemeResource(R.color.background)
            setDistanceToTriggerSync(300)

            setOnRefreshListener {
                refreshInvitations()
            }
        }
    }

    private fun setupAdapter() {
        adapter = InboxAdapter(
            data = mutableListOf(),
            onAcceptListener = { code, invite ->
                handleAcceptInvite(code, invite)
            },
            onRejectListener = { invite, position ->
                handleRejectInvite(invite, position)
            }
        )
        binding.recycler.apply {
            adapter = this@InboxActivity.adapter
            setHasFixedSize(false)
            itemAnimator?.changeDuration = 300
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            logoutBtn.setOnClickListener {
                handleLogout()
            }

            pendingStoreCard.setOnClickListener {
                navigateToMain()
            }
        }
    }

    private fun observeViewModel() {
        // Observe messages
        viewModel.msg.observe(this) { message ->
            message?.let { showToast(it) }
        }

        // Observe invitations list
        viewModel.invites.observe(this) { invites ->
            updateInvitesList(invites)
        }
    }

    private fun checkUserAuthentication() {
        viewModel.validToGoHome(
            goToMain = {
                navigateToMain()
            }
        ) { name, photo ->
            showPendingStore(name, photo)
        }
    }

    private fun showPendingStore(name: String, photo: String?) {
        binding.apply {
            pendingStoreCard.isVisible = true
            storeName.text = name

            Glide.with(this@InboxActivity)
                .load(photo)
                .placeholder(R.drawable.nav_store)
                .error(R.drawable.nav_store)
                .circleCrop()
                .into(storeIcon)
        }
    }

    private fun refreshInvitations() {
        // Disable swipe refresh temporarily to prevent multiple requests
        binding.swipeRefresh.isRefreshing = true

        viewModel.getAllPendingInvitations()

        // Fallback timeout in case the observer doesn't trigger
        binding.swipeRefresh.postDelayed({
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
                showToast(getString(R.string.complete_setup))
            }
        }, 3000)
    }

    private fun updateInvitesList(invites: List<Invitation>) {
        // Stop refreshing animation
        binding.swipeRefresh.isRefreshing = false

        if (invites.isNotEmpty()) {
            adapter.update(invites)
            binding.inviteCount.text = invites.size.toString()
            updateEmptyState(show = false)
        } else {
            updateEmptyState(show = true)
            binding.inviteCount.text = "0"
        }
    }

    private fun updateEmptyState(show: Boolean) {
        binding.apply {
            emptyState.isVisible = show
            recycler.isVisible = !show
        }
    }

    private fun handleAcceptInvite(code: String, invite: Invitation) {
        showLoadingState(true)

        viewModel.accept(
            invite = invite,
            code = code
        ) {
            showLoadingState(false)
            showToast(getString(R.string.invite_accepted))
            navigateToMain()
        }
    }

    private fun handleRejectInvite(invite: Invitation, position: Int) {
        DialogBuilder.showAlertDialog(
            context = this,
            title = getString(R.string.reject_invite_title),
            message = getString(R.string.reject_invite_message),
            positiveButton = getString(R.string.reject),
            negativeButton = getString(R.string.cancel),
            onConfirm = {
                rejectInvite(invite, position)
            },
            onCancel = {
                // User cancelled
            }
        )
    }

    private fun rejectInvite(invite: Invitation, position: Int) {
        showLoadingState(true)

        viewModel.reject(invite) {
            showLoadingState(false)
            showToast(getString(R.string.invite_rejected))

            // Remove item from adapter
            adapter.deleteItem(position)

            // Update count and show empty state if needed
            val newCount = adapter.itemCount
            binding.inviteCount.text = newCount.toString()

            if (newCount == 0) {
                updateEmptyState(show = true)
            }
        }
    }

    private fun handleLogout() {
        DialogBuilder.showAlertDialog(
            context = this,
            title = getString(R.string.logout),
            message = "Are you sure you want to logout?",
            positiveButton = getString(R.string.logout),
            negativeButton = getString(R.string.cancel),
            onConfirm = {
                performLogout()
            },
            onCancel = {
                // User cancelled
            }
        )
    }

    private fun performLogout() {
        showLoadingState(true)

        viewModel.logout { success, message ->
            showLoadingState(false)

            if (success) {
                navigateToLogin()
            } else {
                showToast(message)
            }
        }
    }

    private fun showLoadingState(show: Boolean) {
        binding.swipeRefresh.isRefreshing = show
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun showToast(message: String) {
        if (message.isNotBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}