package com.example.htopstore.ui.staff

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.remoteModels.Invite
import com.example.domain.util.IdGenerator
import com.example.htopstore.databinding.FragmentInvitesBinding
import com.example.htopstore.util.adapters.InvitesAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@Suppress("DEPRECATION")
class InvitesFragment : Fragment() {

    private lateinit var binding: FragmentInvitesBinding
    private lateinit var adapter: InvitesAdapter
    private val vm: StaffViewModel by activityViewModels()

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
        observeInvites()
        setupCreateInviteButton()

        return binding.root
    }

    // ------------------------------------------------------------------------
    // UI Setup
    // ------------------------------------------------------------------------
    private fun setupRecyclerView() {
        adapter = InvitesAdapter(
            data = mutableListOf(),
            onDelete = { invite ->
                showDeleteInviteDialog(invite)
            },
            onShare = { invite ->
                shareInviteCode(invite.code)
            },
            onCopy = { code ->
                copyToClipboard(code)
            }
        )

        binding.recyclerView.adapter = adapter
    }

    private fun setupCreateInviteButton() {
        binding.createInvite.setOnClickListener {
            val email = binding.emailET.text.toString().trim()

            when {
                email.isEmpty() -> {
                    showToast("Please enter an email")
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Please enter a valid email")
                }
                else -> {
                    vm.addInvite(
                        email = email,
                        code = IdGenerator.generateTimestampedId(7)
                    ) {
                        binding.emailET.text = null
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------
    private fun observeInvites() {
        vm.getInvites()

        lifecycleScope.launch {
            vm.invites.collect { list ->
                adapter.updateList(list.toMutableList())
            }
        }
    }

    // ------------------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------------------
    private fun showDeleteInviteDialog(invite: Invite) {
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            title = "Delete Invite",
            message = "Are you sure you want to delete this invite?",
            positiveButton = "Delete",
            negativeButton = "Cancel",
            onConfirm = { vm.deleteInvite(invite) {} },
            onCancel = {}
        )
    }

    private fun shareInviteCode(code: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, code)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Code", text)
        clipboard.setPrimaryClip(clip)
        showToast("Code copied to clipboard")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
