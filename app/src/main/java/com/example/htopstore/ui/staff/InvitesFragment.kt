package com.example.htopstore.ui.staff

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.IdGenerator
import com.example.htopstore.databinding.FragmentInvitesBinding
import com.example.htopstore.util.adapters.InvitesAdapter
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Fragment for managing store invitations.
 * Features: Create invites, filter by status, share/copy codes, send emails
 */
@AndroidEntryPoint
class InvitesFragment : Fragment() {

    private var _binding: FragmentInvitesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffViewModel by activityViewModels()
    private lateinit var adapter: InvitesAdapter

    // State management
    private val selectedFilter = MutableStateFlow(FilterType.ALL)
    private var allInvites = emptyList<Invitation>()

    private companion object {
        const val INVITE_CODE_LENGTH = 7
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvitesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupCreateInviteSection()
        observeInvites()
        observeLoadingState()

        // Initial data load
        viewModel.getAllStoreInvites()
    }

    /**
     * Setup RecyclerView with adapter and callbacks
     */
    private fun setupRecyclerView() {
        adapter = InvitesAdapter(
            onDelete = ::showDeleteInviteDialog,
            onShare = ::shareInviteCode,
            onCopy = ::copyToClipboard,
            onSending = { code, email ->
                if (code != null && email != null) {
                    viewModel.sendEmail(code){ subject, emailBody ->
                        sendEmail(subject,emailBody,email)
                    }
                }
            }
        )
        binding.recyclerView.adapter = adapter
    }
    private fun sendEmail(sub:String, body: String,email:String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$email".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, sub)
            putExtra(Intent.EXTRA_TEXT, body )
        }
        startActivity(intent)
    }
    /**
     * Setup filter chips
     */
    private fun setupFilterChips() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.ALL
        }

        binding.chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.PENDING
        }

        binding.chipAccepted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.ACCEPTED
        }

        binding.chipRejected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.REJECTED
        }

        // Observe filter changes
        viewLifecycleOwner.lifecycleScope.launch {
            selectedFilter.collect { filter ->
                applyFilter(filter)
            }
        }
    }

    /**
     * Setup create invite section with validation
     */
    private fun setupCreateInviteSection() {
        // Real-time email validation
        binding.emailET.addTextChangedListener { text ->
            validateEmail(text?.toString()?.trim())
        }

        binding.createInvite.setOnClickListener {
            handleCreateInvite()
        }
    }

    /**
     * Validate email format
     */
    private fun validateEmail(email: String?) {
        when {
            email.isNullOrEmpty() -> {
                binding.emailLayout.error = null
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = "Invalid email format"
            }
            else -> {
                binding.emailLayout.error = null
            }
        }
    }

    /**
     * Handle create invite button click
     */
    private fun handleCreateInvite() {
        val email = binding.emailET.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.emailLayout.error = "Email is required"
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = "Please enter a valid email"
            }
            else -> {
                binding.emailLayout.error = null
                createInvite(email)
            }
        }
    }

    /**
     * Create a new invitation
     */
    private fun createInvite(email: String) {
        val inviteCode = IdGenerator.generateTimestampedId(INVITE_CODE_LENGTH)

        viewModel.addInvite(
            email = email,
            code = inviteCode
        ) {
            binding.emailET.text?.clear()
        }
    }

    /**
     * Observe invites list changes
     */
    private fun observeInvites() {
        viewModel.invites.observe(viewLifecycleOwner) { inviteList ->
            allInvites = inviteList

            if (inviteList.isEmpty()) {
                showEmptyState(true)
                updateInviteCount(0)
            } else {
                showEmptyState(false)
                applyFilter(selectedFilter.value)
            }
        }
    }

    /**
     * Observe loading state
     */
    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleLoadingState(isLoading)
        }
    }

    /**
     * Observe messages from ViewModel
     */


    /**
     * Apply filter to invitations list
     */
    private fun applyFilter(filter: FilterType) {
        val filteredInvites = when (filter) {
            FilterType.ALL -> allInvites
            FilterType.PENDING -> allInvites.filter {
                it.status == STATUS_PENDING
            }
            FilterType.ACCEPTED -> allInvites.filter {
                it.status == STATUS_ACCEPTED
            }
            FilterType.REJECTED -> allInvites.filter {
                it.status != STATUS_PENDING && it.status != STATUS_ACCEPTED
            }
        }

        adapter.submitList(filteredInvites)
        updateInviteCount(filteredInvites.size)

        // Handle empty filtered results
        val shouldShowEmptyState = filteredInvites.isEmpty() && allInvites.isNotEmpty()
        showEmptyState(shouldShowEmptyState, isFilterResult = true)
    }

    /**
     * Show delete confirmation dialog
     */
    private fun showDeleteInviteDialog(invite: Invitation) {
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            title = "Delete Invite",
            message = "Are you sure you want to delete the invite for ${invite.email}?",
            positiveButton = "Delete",
            negativeButton = "Cancel",
            onConfirm = {
                viewModel.deleteInvite(invite) {}
            },
            onCancel = {}
        )
    }

    /**
     * Share invite code using system share dialog
     */
    private fun shareInviteCode(invite: Invitation) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Store Invite Code")
            putExtra(Intent.EXTRA_TEXT, "Your invite code: ${invite.code}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share invite code"))
    }

    /**
     * Copy invite code to clipboard
     */
    private fun copyToClipboard(text: String) {
        val clipboard = requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Code", text)
        clipboard.setPrimaryClip(clip)
        showSnackbar("Code copied to clipboard")
    }

    /**
     * Update invite count text
     */
    private fun updateInviteCount(count: Int) {
        binding.inviteCount.text = when (count) {
            0 -> "No invites sent"
            1 -> "1 invite sent"
            else -> "$count invites sent"
        }
    }

    /**
     * Toggle empty state visibility
     */
    private fun showEmptyState(show: Boolean, isFilterResult: Boolean = false) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    /**
     * Toggle loading state
     */
    private fun toggleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Show snackbar message
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Filter types for invitation list
     */
    enum class FilterType {
        ALL,
        PENDING,
        ACCEPTED,
        REJECTED
    }
}