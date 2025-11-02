package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.htopstore.R
import com.example.htopstore.databinding.TopItemBinding

class MostProfitableAdapter(private val data: MutableList<Product>,private val onClick: (Product) -> Unit) :
    RecyclerView.Adapter<MostProfitableAdapter.MHolder>() {

    // Create ViewHolder class
    class MHolder(val binding: TopItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MHolder {
        val binding = TopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: MHolder, position: Int) {
        val product = data[position]
        Glide.with(holder.binding.productImg)
            .load(product.productImage)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(holder.binding.productImg)
        holder.binding.Name.text = product.name
        holder.binding.category.text = product.category
        holder.binding.quantity.text = ((product.sellingPrice - product.buyingPrice)*product.soldCount).toString()
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