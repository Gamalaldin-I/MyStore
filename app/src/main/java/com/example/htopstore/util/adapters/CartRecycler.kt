package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.CartProduct
import com.example.domain.useCase.localize.GetCategoryLocalName
import com.example.domain.useCase.localize.NAE.ae
import com.example.htopstore.R
import com.example.htopstore.databinding.CartItemBinding

class CartRecycler(private val data: ArrayList<CartProduct>, val onDelete:(item: CartProduct)-> Unit, val onIncOrDec :() ->Unit) :
    RecyclerView.Adapter<CartRecycler.pHolder>() {
        val catTrans = GetCategoryLocalName()

    // Create ViewHolder class
    class pHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): pHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return pHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: pHolder, position: Int) {
        val currentItem = data[position]
        holder.binding.productBrand.text = currentItem.name
        holder.binding.productType.text = catTrans(currentItem.type)
        val imagePath = currentItem.image
        val context = holder.binding.productImg.context
        Glide.with(context)
            .load(imagePath)
            .placeholder(R.drawable.ic_camera)
            .error(R.drawable.ic_camera)
            .into(holder.binding.productImg)
        holder.binding.price.text = currentItem.pricePerOne.toInt().ae()
        holder.binding.count.text = currentItem.sellingCount.ae()
        holder.binding.increment.setOnClickListener{
            if (currentItem.sellingCount == currentItem.maxLimitCount) {
                return@setOnClickListener
            }
            currentItem.sellingCount++
            holder.binding.count.text = currentItem.sellingCount.ae()
            holder.binding.price.text = (currentItem.pricePerOne * currentItem.sellingCount).toInt().ae()
            onIncOrDec()
        }
        holder.binding.decrement.setOnClickListener{
            if (currentItem.sellingCount > 1){
                currentItem.sellingCount--
                holder.binding.count.text = currentItem.sellingCount.ae()
                holder.binding.price.text = (currentItem.pricePerOne * currentItem.sellingCount).toInt().ae()
                 }
            else {
                data.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, data.size)
                onDelete(currentItem)
            }
            onIncOrDec()

    }}

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<CartProduct>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}