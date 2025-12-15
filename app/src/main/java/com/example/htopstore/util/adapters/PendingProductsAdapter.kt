package com.example.htopstore.util.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.Product
import com.example.htopstore.databinding.PendingProductCardBinding

class PendingProductsAdapter(private val data: ArrayList<Product>,private val onDelete:(String,Int)->Unit) :
    RecyclerView.Adapter<PendingProductsAdapter.PHolder>() {

    // Create ViewHolder class
    class PHolder(val binding: PendingProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PHolder {
        val binding = PendingProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PHolder(binding)
    }
    fun onDelete(index:Int){
        data.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, data.size)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: PHolder, position: Int) {
        val d = data[position]
        holder.binding.apply {
            deleteBtn.setOnClickListener {
                onDelete(d.id,position)
            }
            tvProductName.text = d.name
            tvQuantity.text = d.count.toString()
            tvUnitPrice.text = d.sellingPrice.toString()
            val imageUri = d.productImage.toUri()
            ivProductImage.setImageURI(imageUri)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}