package com.example.htopstore.util.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.remoteModels.Invite
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.htopstore.R
import com.example.htopstore.databinding.InviteCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvitesAdapter(
    private val onDelete: (invite: Invite) -> Unit,
    private val onShare: (invite: Invite) -> Unit,
    private val onCopy: (text: String) -> Unit
) : ListAdapter<Invite, InvitesAdapter.InviteHolder>(DiffCallback()) {

    class InviteHolder(val binding: InviteCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteHolder {
        val binding = InviteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InviteHolder(binding)
    }

    override fun onBindViewHolder(holder: InviteHolder, position: Int) {
        val invite = getItem(position)

        with(holder.binding) {
            // Email
            email.text = invite.email ?: "No email"

            // Status Chip
            val status = invite.status ?: "Unknown"
            statusChip.apply {
                text = status.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }

                when (status) {
                    STATUS_PENDING -> {
                        setTextColor(context.getColor(R.color.process_pending))
                    }
                    STATUS_ACCEPTED -> {
                        setTextColor(context.getColor(R.color.process_approved))
                    }
                    else -> {
                        setTextColor(context.getColor(R.color.process_rejected))
                    }
                }
            }

            // Code
            code.text = invite.code ?: "N/A"

            // Created At - Format timestamp
            createdAt.text = invite.createdAt

            // Action Buttons
            shareBtn.setOnClickListener { onShare(invite) }
            deleteBtn.setOnClickListener { onDelete(invite) }
            copyBtn.setOnClickListener {
                invite.code?.let { code -> onCopy(code) }
            }
        }
    }

    private fun formatDate(timestamp: Long?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown date"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Invite>() {
        override fun areItemsTheSame(oldItem: Invite, newItem: Invite): Boolean =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: Invite, newItem: Invite): Boolean =
            oldItem == newItem
    }
}