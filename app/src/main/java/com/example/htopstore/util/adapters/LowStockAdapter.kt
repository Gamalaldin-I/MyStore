package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.htopstore.R
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.databinding.LowStockItemBinding

class LowStockAdapter(private val data: MutableList<Product>,private val onClick: (Product) -> Unit) :
    RecyclerView.Adapter<LowStockAdapter.THolder>() {

    // Create ViewHolder class
    class THolder(val binding: LowStockItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): THolder {
        val binding = LowStockItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return THolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: THolder, position: Int) {
        val product = data[position]
        Glide.with(holder.binding.productImg)
            .load(product.productImage)
            .error(R.drawable.fighter)
            .placeholder(R.drawable.fighter)
            .into(holder.binding.productImg)
        holder.binding.Name.text = product.name
        holder.binding.category.text = product.category
        holder.binding.quantity.text = product.count.toString()
        holder.binding.root.setOnClickListener {
            onClick(product)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Product>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()

    }
}