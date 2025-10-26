package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.SoldProduct
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
        holder.binding.priceUnit.text = "${item.sellingPrice} $"
        holder.binding.date.text = item.sellDate
        holder.binding.time.text = item.sellTime
            // Return
        val absQuantity = -item.quantity
        holder.binding.count.text = "Qty: $absQuantity"
        holder.binding.total.text = "${item.sellingPrice * absQuantity} $"
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
