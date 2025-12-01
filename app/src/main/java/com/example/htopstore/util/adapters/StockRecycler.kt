package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.Product
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.CartHelper
import com.example.htopstore.R
import com.example.htopstore.databinding.ItemCardBinding
import com.example.htopstore.util.helper.Animator.animateAddToCart
import com.example.htopstore.util.helper.Animator.animateStockItem

class StockRecycler(private val onProductClick: (Product) -> Unit):
    ListAdapter<Product, StockRecycler.PHolder>(DiffCallback()) {
    class PHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            val context = holder.binding.productImg.context
            val url = item.productImage

            // Glide auto-detect (URL, File path, or Uri string)
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .into(productImg)

            productBrand.text = item.name
            productPrice.text = item.sellingPrice.toInt().ae()
            productType.text = item.category
            count.text = "${item.count}/${(item.count + item.soldCount)}"

            addToCart.setOnClickListener {
                addToCart.animateAddToCart{
                CartHelper.addToTheCartList(item)
                }
            }
            root.setOnClickListener {
                root.animateStockItem {
                    Log.d("DEBUGRES1", "Item clicked: $item")
                    onProductClick(item)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}