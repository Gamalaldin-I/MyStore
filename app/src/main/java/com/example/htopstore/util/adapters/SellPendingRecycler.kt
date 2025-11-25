package com.example.htopstore.util.adapters
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.PendingSellAction
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SellPendingHolder, position: Int) {
        val data = getItem(position)
        val binding = holder.binding

        // Sale ID
        binding.pendingSellTitle.text = "Sale #${data.id}"

        // Total Cash
        binding.totalCash.text = "$${getTotalCash(data)}"

        // Bill Status Icon
        if (data.billInserted) {
            binding.billStatusIcon.setImageResource(R.drawable.ic_check_circle)
            binding.billStatusIcon.setColorFilter(
                holder.itemView.context.getColor(R.color.primary_light)
            )
        } else {
            binding.billStatusIcon.setImageResource(R.drawable.ic_error_circle)
            binding.billStatusIcon.setColorFilter(
                holder.itemView.context.getColor(R.color.input_error)
            )
        }

        // Items Status Icon
        if (data.soldItemsInserted) {
            binding.itemsStatusIcon.setImageResource(R.drawable.ic_check_circle)
            binding.itemsStatusIcon.setColorFilter(
                holder.itemView.context.getColor(R.color.primary_light)
            )
        } else {
            binding.itemsStatusIcon.setImageResource(R.drawable.ic_error_circle)
            binding.itemsStatusIcon.setColorFilter(
                holder.itemView.context.getColor(R.color.input_error)
            )
        }

        // Status Badge
        binding.status.text = data.status

        // First Product Image
        Glide.with(binding.firstProductImage.context)
            .load(data.soldProducts.firstOrNull()?.image)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(binding.firstProductImage)

        // Progress Bar
        binding.progress.progress = data.progress
        binding.progressText.text = "Syncing... ${data.progress}%"
    }

    private fun getTotalCash(pendingAction: PendingSellAction): Int {
        return pendingAction.soldProducts.sumOf {
            (it.pricePerOne * it.sellingCount).toInt()
        }
    }

    // DiffUtil Callback
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

    // Update Progress for specific item
    fun updateProgress(id: Int, progress: Int) {
        val currentListCopy = currentList.toMutableList()
        val index = currentListCopy.indexOfFirst { it.id == id }

        if (index != -1) {
            val updated = currentListCopy[index].copy(progress = progress)
            currentListCopy[index] = updated
            submitList(currentListCopy) {
                notifyItemChanged(index)
            }
        }
    }
}
