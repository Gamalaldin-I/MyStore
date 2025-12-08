package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.domain.model.Notification
import com.example.domain.util.NotificationManager
import com.example.domain.util.NotificationTimeUtils
import com.example.htopstore.R
import com.example.htopstore.databinding.NotificationCardBinding

class NotificationsAdapter(
    private val onItemClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationsAdapter.NHolder>(NotificationDiffCallback()) {

    inner class NHolder(private val binding: NotificationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(notification: Notification) {
            with(binding) {
                // User photo
                Glide.with(root.context)
                    .load(notification.userImage)
                    .placeholder(R.drawable.icon_profile)
                    .error(R.drawable.icon_profile)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userPhoto)

                // Type label & badge color
                typeLabel.text = NotificationManager.getNotificationTypeLabel(notification.type)
                typeBadge.setColorFilter(NotificationManager.getNotificationColor(notification.type).toColorInt())

                // Professional time display
                timeTextView.text = NotificationTimeUtils.getRelativeTime(notification.createdAt)

                // Description
                descriptionTextView.text = notification.description

                // Product name (conditional visibility)
                if (notification.productName.isNotEmpty()) {
                    productName.visibility = View.VISIBLE
                    productName.text = "Product: ${notification.productName}"
                } else {
                    productName.visibility = View.GONE
                }

                // Card styling based on deleted status
                cardView.strokeWidth = if (!notification.deleted) 2 else 1
                cardView.strokeColor = if (!notification.deleted) {
                    ContextCompat.getColor(root.context, R.color.action_success)
                } else {
                    ContextCompat.getColor(root.context, android.R.color.darker_gray)
                }

                // Click listener
                root.setOnClickListener { onItemClick(notification) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NHolder {
        val binding = NotificationCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NHolder(binding)
    }

    override fun onBindViewHolder(holder: NHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem == newItem
    }
}