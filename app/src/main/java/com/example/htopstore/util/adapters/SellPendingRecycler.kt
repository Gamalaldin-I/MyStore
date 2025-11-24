package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.data.local.model.entities.PendingSellAction
import com.example.domain.util.Constants
import com.example.htopstore.R
import com.example.htopstore.databinding.SellPendingActionCardBinding

class SellPendingRecycler :
    ListAdapter<PendingSellAction, SellPendingRecycler.SellPendingHolder>(DiffCallback()) {

    class SellPendingHolder(val binding: SellPendingActionCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellPendingHolder {
        val binding = SellPendingActionCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SellPendingHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: SellPendingHolder, position: Int) {
        val d = getItem(position)
        val b = holder.binding

        b.pendingSellTitle.text = d.id.toString()
        b.totalCash.text = "${getTotalCash(d)}$"

        // Bill status
        if (d.billInserted) {
            b.billStatusIcon.setImageResource(R.drawable.ic_check_circle)
            b.billStatusIcon.setColorFilter(R.color.primary_light)
        } else {
            b.billStatusIcon.setImageResource(R.drawable.ic_error_circle)
            b.billStatusIcon.setColorFilter(R.color.input_error)
        }

        // Items status
        if (d.soldItemsInserted) {
            b.itemsStatusIcon.setImageResource(R.drawable.ic_check_circle)
            b.itemsStatusIcon.setColorFilter(R.color.primary_light)
        } else {
            b.itemsStatusIcon.setImageResource(R.drawable.ic_error_circle)
            b.itemsStatusIcon.setColorFilter(R.color.input_error)
        }

        // Status Badge
        b.status.text = d.status
        b.status.setTextColor(
            if (d.status == Constants.STATUS_PENDING) R.color.process_pending else R.color.action_primary
        )
        b.statusBadge.strokeColor =
            if (d.status == Constants.STATUS_PENDING) R.color.process_pending else R.color.action_primary

        // First Image
        Glide.with(b.firstProductImage.context)
            .load(d.soldProducts.firstOrNull()?.image)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(b.firstProductImage)

        // Progress
        b.progress.progress = d.progress
        b.progressText.text = "Syncing ...${d.progress}%"
    }

    private fun getTotalCash(p: PendingSellAction): Int {
        var sum = 0
        p.soldProducts.forEach {
            sum += (it.pricePerOne * it.sellingCount).toInt()
        }
        return sum
    }

    // -------------------- DIFF UTIL ---------------------
    class DiffCallback : DiffUtil.ItemCallback<PendingSellAction>() {
        override fun areItemsTheSame(
            oldItem: PendingSellAction,
            newItem: PendingSellAction
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PendingSellAction,
            newItem: PendingSellAction
        ): Boolean {
            return oldItem == newItem
        }
    }

    // ----------------- Update Progress Safely -----------------
    fun updateProgress(id: Int, progress: Int) {
        val currentListCopy = currentList.toMutableList()
        val index = currentListCopy.indexOfFirst { it.id == id }

        if (index != -1) {
            val updated = currentListCopy[index].copy(progress = progress)
            currentListCopy[index] = updated
            submitList(currentListCopy)
        }
    }
}
