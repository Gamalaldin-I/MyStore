package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.SoldProduct
import com.example.htopstore.R
import com.example.htopstore.databinding.ReturnItemBinding

class ReturnsAdapter(private val data: ArrayList<SoldProduct>,val onItemClick:(item:SoldProduct)->Unit) :
    RecyclerView.Adapter<ReturnsAdapter.RHolder>() {

    class RHolder(val binding: ReturnItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RHolder {
        val binding = ReturnItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RHolder, position: Int) {
        val item = data[position]
        val context = holder.binding.root.context

        holder.binding.pos.text = (position + 1).toString()
        holder.binding.brandName.text = "${item.name} (${item.type})"
        holder.binding.priceUnit.text = "UP: ${item.sellingPrice} $"
        holder.binding.date.text = item.sellDate
        holder.binding.time.text = item.sellTime

        if (item.quantity > 0) {
            // Sold
            holder.binding.count.text = "Quantity: ${item.quantity}"
            holder.binding.total.text = "Total: ${item.sellingPrice * item.quantity} $"
            holder.binding.container.setBackgroundResource(R.drawable.sold_bg)
            holder.binding.total.setTextColor(
                ContextCompat.getColor(context, R.color.revenue_positive)
            )
        } else {
            // Return
            val absQuantity = -item.quantity
            holder.binding.count.text = "Return: $absQuantity"
            holder.binding.total.text = "Total: ${item.sellingPrice * absQuantity} $"
            holder.binding.total.setTextColor(
                ContextCompat.getColor(context, R.color.revenue_negative)
            )
            holder.binding.container.setBackgroundResource(R.drawable.return_bg)
        }
        holder.binding.root.setOnClickListener {
            onItemClick(data[position])
        }
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SoldProduct>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}
