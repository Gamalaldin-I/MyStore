package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.domain.useCase.localize.NAE.ae
import com.example.htopstore.R
import com.example.htopstore.databinding.LowStockItemBinding

class LowStockAdapter(private val data: MutableList<Product>, private val forLow: Boolean = true, private val onClick: (Product) -> Unit) :
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
        if(!forLow){
            holder.binding.cover.setBackgroundResource(R.drawable.have_not_sold_cover)
        }
        Glide.with(holder.binding.productImg)
            .load(product.productImage)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(holder.binding.productImg)
        holder.binding.Name.text = product.name
        holder.binding.category.text = product.category
        holder.binding.quantity.text = product.count.ae()
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