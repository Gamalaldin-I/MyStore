package com.example.htopstore.util.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.NotificationTimeUtils
import com.example.htopstore.R
import com.example.htopstore.databinding.InviteCardBinding

class InvitesAdapter(
    private val onDelete: (invite: Invitation) -> Unit,
    private val onShare: (invite: Invitation) -> Unit,
    private val onCopy: (text: String) -> Unit,
    private val onSending:(code:String?,email:String?)->Unit
) : ListAdapter<Invitation, InvitesAdapter.InviteHolder>(DiffCallback()) {

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
                val context = this.context
                when (status) {
                    STATUS_PENDING -> {
                        setTextColor(context.getColor(R.color.process_pending))
                        text = getString(context,R.string.pending)
                    }
                    STATUS_ACCEPTED -> {
                        setTextColor(context.getColor(R.color.process_approved))
                        text = getString(context,R.string.accepted)
                    }
                    else -> {
                        setTextColor(context.getColor(R.color.process_rejected))
                        text = getString(context,R.string.rejected)
                    }
                }
            }

            // Code
            code.text = invite.code ?: "N/A"

            // Created At - Format timestamp
            createdAt.text = NotificationTimeUtils.getRelativeTime(invite.createdAt!!)

            // Action Buttons
            shareBtn.setOnClickListener { onShare(invite) }
            deleteBtn.setOnClickListener { onDelete(invite) }
            copyBtn.setOnClickListener {
                invite.code?.let { code -> onCopy(code) }
            }
            email.setOnClickListener{
                onSending(invite.code,invite.email)
            }
        }
    }



    class DiffCallback : DiffUtil.ItemCallback<Invitation>() {
        override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation): Boolean =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation): Boolean =
            oldItem == newItem
    }
    // send the invitation to the employee a cross a mail

}