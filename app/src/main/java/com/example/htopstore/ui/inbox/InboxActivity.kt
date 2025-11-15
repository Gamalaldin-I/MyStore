package com.example.htopstore.ui.inbox

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.model.remoteModels.Invitation
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityInboxBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.util.adapters.InboxAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxActivity : AppCompatActivity() {

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
        loadInvitations()
    }

    private fun setupUI() {
        binding.apply {
            myEmail.text = viewModel.getEmail()
            emptyState.visibility = View.VISIBLE
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
        binding.recycler.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.logoutBtn.setOnClickListener {
            handleLogout()
        }
    }

    private fun observeViewModel() {
        // Observe messages
        viewModel.msg.observe(this) { message ->
            showToast(message)
        }

        // Observe invitations list
            viewModel.invites.observe(this) { invites ->
                updateInvitesList(invites)
            }
    }

    private fun checkUserAuthentication() {
        viewModel.validToGoHome {
            navigateToMain()
        }
    }

    private fun loadInvitations() {
        viewModel.getAllPendingInvitations()
    }

    private fun refreshInvitations() {
        viewModel.getAllPendingInvitations()

        // Stop refreshing after a short delay if no callback is available
        binding.swipeRefresh.postDelayed({
            binding.swipeRefresh.isRefreshing = false
        }, 1500)
    }

    private fun updateInvitesList(invites: List<Invitation>) {
        binding.swipeRefresh.isRefreshing = false

        if (invites.isNotEmpty()) {
            adapter.update(invites)
            binding.inviteCount.text = invites.size.toString()
            binding.emptyState.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.VISIBLE
            binding.inviteCount.text = "0"
        }
    }

    private fun handleAcceptInvite(code: String, invite: Invitation) {
        viewModel.accept(
            invite = invite,
            code = code
        ) {
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
                // Do nothing
            }
        )
    }

    private fun rejectInvite(invite: Invitation, position: Int) {
        viewModel.reject(invite) {
            showToast(getString(R.string.invite_rejected))
            // Optionally remove the item from adapter
             adapter.deleteItem(position)
            if (adapter.itemCount == 0) {
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun handleLogout() {
        viewModel.logout { success, message ->
            if (success) {
                navigateToLogin()
            } else {
                showToast(message)
            }
        }
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}