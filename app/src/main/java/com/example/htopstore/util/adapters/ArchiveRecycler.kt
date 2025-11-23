package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.domain.useCase.localize.GetCategoryLocalName
import com.example.htopstore.R
import com.example.htopstore.databinding.ArchiveItemBinding

class ArchiveRecycler(private val data: ArrayList<Product>,
                      private val onClick:(p: Product)->Unit) :
    RecyclerView.Adapter<ArchiveRecycler.pHolder>(){
        val categoryTrans = GetCategoryLocalName()

    // Create ViewHolder class
    class pHolder(val binding: ArchiveItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): pHolder {
        val binding = ArchiveItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return pHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: pHolder, position: Int) {
       val d = data[position] ; val b = holder.binding
        b.price.text = d.sellingPrice.toString()
        Glide.with(b.productImg.context)
            .load(d.productImage)
            .placeholder(R.drawable.ic_camera)
            .error(R.drawable.circle_background)
            .into(b.productImg)
        b.productBrand.text = d.name
        b.productType.text = categoryTrans(d.category)
        holder.itemView.setOnClickListener {
            onClick(d)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateAfterDeletion(position:Int){
        data.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, data.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTheList(list:ArrayList<Product>){
        this.data.clear()
        this.data.addAll(list)
        notifyDataSetChanged()
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}