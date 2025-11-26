package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.CartProduct
import com.example.htopstore.R
import com.example.htopstore.databinding.PendingSoldItemBinding

class PendingCartAdapter(private val data: MutableList<CartProduct>) : RecyclerView.Adapter<PendingCartAdapter.CHolder>() {

    // Create ViewHolder class
    class CHolder(val binding: PendingSoldItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CHolder {
        val binding=PendingSoldItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: CHolder, position: Int) {
        val item=data[position]
        holder.binding.tvProductName.text=item.name
        holder.binding.tvQuantity.text=item.sellingCount.toString()
        holder.binding.tvUnitPrice.text=item.pricePerOne.toString()
        holder.binding.tvItemPrice.text=(item.pricePerOne*item.sellingCount).toString()
        Glide.with(holder.binding.ivProductImage.context)
            .load(item.image)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(holder.binding.ivProductImage)
    }
    fun getTotalOrderPriceAndTotalAfterDiscount(discount:Int):Pair<Double,Double>{
        if(data.isEmpty()) return Pair(0.0,0.0)
        var totalPrice=0.0
        for (item in data){
            totalPrice+=item.pricePerOne*item.sellingCount
        }
        if(discount == 0) return Pair(totalPrice,totalPrice)
        val discountAmount = totalPrice * (discount / 100.0)
        val total = totalPrice - discountAmount
        return Pair(totalPrice,total)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<CartProduct>){
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }


    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}