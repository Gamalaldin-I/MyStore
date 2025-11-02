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
import com.example.domain.model.remoteModels.Invite
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

@AndroidEntryPoint
class InvitesFragment : Fragment() {

    private lateinit var binding: FragmentInvitesBinding

    private lateinit var adapter: InvitesAdapter
    private val vm: StaffViewModel by activityViewModels()

    private val selectedFilter = MutableStateFlow(FilterType.ALL)
    private var allInvites = listOf<Invite>()

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentInvitesBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupFilters()
        setupCreateInviteButton()
        observeInvites()

        return binding.root
    }

    // ------------------------------------------------------------------------
    // UI Setup
    // ------------------------------------------------------------------------
    private fun setupRecyclerView() {
        adapter = InvitesAdapter(
            onDelete = { invite -> showDeleteInviteDialog(invite) },
            onShare = { invite -> shareInviteCode(invite.code) },
            onCopy = { code -> copyToClipboard(code) },
            onSending = { code, email ->
                if (code != null && email != null) {
                    val (subject, message ) = vm.sendEmail(requireContext(), email, code)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    try { startActivity(Intent.createChooser(intent, "Send invitation via"))
                    } catch (e: Exception) {
                        // Handle case where no email app is installed
                        e.printStackTrace()
                    }
                } else{
                    showSnackBar("Error sending invitation")
                }
                showSnackBar("Invitation sent successfully")
            }
        )

        binding.recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFilter.value = FilterType.ALL
            }
        }

        binding.chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFilter.value = FilterType.PENDING
            }
        }

        binding.chipAccepted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFilter.value = FilterType.ACCEPTED
            }
        }

        binding.chipRejected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFilter.value = FilterType.REJECTED
            }
        }

        // Observe filter changes
        viewLifecycleOwner.lifecycleScope.launch {
            selectedFilter.collect { filter ->
                filterInvites(filter)
            }
        }
    }

    private fun setupCreateInviteButton() {
        // Real-time email validation
        binding.emailET.addTextChangedListener { text ->
            val email = text?.toString()?.trim() ?: ""
            if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLayout.error = "Invalid email format"
            } else {
                binding.emailLayout.error = null
            }
        }

        binding.createInvite.setOnClickListener {
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
    }

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------
    private fun observeInvites() {
        showLoading(true)
        vm.getInvites()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.invites.collect { list ->
                showLoading(false)
                allInvites = list

                if (list.isEmpty()) {
                    showEmptyState(true)
                    updateInviteCount(0)
                } else {
                    showEmptyState(false)
                    filterInvites(selectedFilter.value)
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Business Logic
    // ------------------------------------------------------------------------
    private fun createInvite(email: String) {
        vm.addInvite(
            email = email,
            code = IdGenerator.generateTimestampedId(7)
        ) {
            binding.emailET.text = null
            showSnackBar("Invite created successfully")
        }
    }

    private fun filterInvites(filter: FilterType) {
        val filteredList = when (filter) {
            FilterType.ALL -> allInvites
            FilterType.PENDING -> allInvites.filter { it.status == STATUS_PENDING }
            FilterType.ACCEPTED -> allInvites.filter { it.status == STATUS_ACCEPTED }
            FilterType.REJECTED -> allInvites.filter {
                it.status != STATUS_PENDING && it.status != STATUS_ACCEPTED
            }
        }

        adapter.submitList(filteredList)
        updateInviteCount(filteredList.size)

        // Show empty state if filtered list is empty but we have invites
        if (filteredList.isEmpty() && allInvites.isNotEmpty()) {
            showEmptyState(true, isFilterResult = true)
        } else if (filteredList.isNotEmpty()) {
            showEmptyState(false)
        }
    }

    // ------------------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------------------
    private fun showDeleteInviteDialog(invite: Invite) {
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            title = "Delete Invite",
            message = "Are you sure you want to delete the invite for ${invite.email}?",
            positiveButton = "Delete",
            negativeButton = "Cancel",
            onConfirm = {
                vm.deleteInvite(invite) {
                    showSnackBar("Invite deleted")
                }
            },
            onCancel = {}
        )
    }

    private fun shareInviteCode(code: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Store Invite Code")
            putExtra(Intent.EXTRA_TEXT, "Your invite code: $code")
        }
        startActivity(Intent.createChooser(shareIntent, "Share invite code"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Code", text)
        clipboard.setPrimaryClip(clip)
        showSnackBar("Code copied to clipboard")
    }

    private fun updateInviteCount(count: Int) {
        binding.inviteCount.text = when (count) {
            0 -> "No invites sent"
            1 -> "1 invite sent"
            else -> "$count invites sent"
        }
    }

    private fun showEmptyState(show: Boolean, isFilterResult: Boolean = false) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    enum class FilterType {
        ALL, PENDING, ACCEPTED, REJECTED
    }
}