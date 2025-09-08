package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htopstore.R
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.databinding.SoldItemBinding
import com.example.htopstore.domain.useCase.CategoryLocalManager

class BillDetailsAdapter(
    private var data: MutableList<SoldProduct>,
    private val onItemClicked: (SoldProduct) -> Unit
) : RecyclerView.Adapter<BillDetailsAdapter.SDHolder>() {

    class SDHolder(val binding: SoldItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SDHolder {
        val binding = SoldItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SDHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SDHolder, position: Int) {
        val item = data[position]
        val context = holder.binding.root.context

        holder.binding.pos.text = (position + 1).toString()
        holder.binding.brandName.text = "${item.name} (${CategoryLocalManager.getCategoryNameLocal(item.type)})"
        holder.binding.priceUnit.text = "UP: ${item.sellingPrice} $"

        if (item.quantity > 0) {
            // Sold
            holder.binding.count.text = "Quantity: ${item.quantity}"
            holder.binding.total.text = "Total: ${item.sellingPrice * item.quantity} $"

            holder.binding.container.setBackgroundResource(R.drawable.bg_receipt)

        } else {
            // Return
            val absQuantity = -item.quantity
            holder.binding.count.text = "Return: $absQuantity"
            holder.binding.total.text = "Total: ${item.sellingPrice * absQuantity} $"

            holder.binding.container.setBackgroundResource(R.drawable.return_bg)

        }

        // Click only if it's a sold item
        holder.binding.root.setOnClickListener {
            if (item.quantity > 0) {
                onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SoldProduct>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
