package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.SelectionQrProduct
import com.example.domain.useCase.localize.GetCategoryLocalName
import com.example.htopstore.R
import com.example.htopstore.databinding.ProductSelectionItemBinding

class QrSelectAdapter(private var data: MutableList<SelectionQrProduct>,val context: Context) :
    RecyclerView.Adapter<QrSelectAdapter.SHolder>() {
    private val selected = ArrayList<SelectionQrProduct>()
    private val catTrans = GetCategoryLocalName()
    // Create ViewHolder class
    class SHolder(val binding: ProductSelectionItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SHolder {
        val binding =
            ProductSelectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: SHolder, position: Int) {
        val item = data[position]
        holder.binding.apply {
            productBrand.text = item.name
            productType.text = catTrans(item.type)
            checked.isChecked = item.selected
        }
        Glide.with(holder.binding.productImg.context)
            .load(item.image)
            .error(R.drawable.ic_camera)
            .placeholder(R.drawable.ic_camera)
            .into(holder.binding.productImg)
        holder.binding.checked.setOnClickListener {
            item.selected = !item.selected
            holder.binding.checked.isChecked = item.selected
            if (item.selected) {
                selected.add(item)
            } else {
                selected.remove(item)
            }
        }
        holder.binding.root.setOnClickListener {
            item.selected = !item.selected
            holder.binding.checked.isChecked = item.selected
            if (item.selected) {
                selected.add(item)
            } else {
                selected.remove(item)
            }
        }

    }

    fun getSelected(): ArrayList<SelectionQrProduct> {
        return selected
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SelectionQrProduct>) {
        this.data.clear()
        this.data.addAll(newData)
        notifyDataSetChanged()
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}