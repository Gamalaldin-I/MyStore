package com.example.htopstore.util

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.databinding.ItemCardBinding
import com.example.htopstore.domain.useCase.CategoryLocalManager
import com.example.htopstore.util.NAE.ae
import java.io.File

class StockRecycler :
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
            val imagePath = item.productImage

            // Glide auto-detect (URL, File path, or Uri string)
            Glide.with(context)
                .load(File(imagePath))
                .placeholder(com.example.htopstore.R.drawable.stock_bg)
                .error(com.example.htopstore.R.drawable.ic_camera)
                .into(productImg)

            productBrand.text = item.name
            productPrice.text = item.sellingPrice.toInt().ae()
            productType.text = CategoryLocalManager.getCategoryNameLocal(item.category)
            count.text = "${item.count}/${(item.count + item.soldCount)}"

            addToCart.setOnClickListener {
                CartHelper.addToTheCartList(item)
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
