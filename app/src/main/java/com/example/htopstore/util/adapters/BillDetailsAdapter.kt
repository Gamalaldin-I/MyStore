package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.SoldProduct
import com.example.domain.useCase.localize.GetCategoryLocalName
import com.example.htopstore.databinding.SoldItemBinding

class BillDetailsAdapter(
    private var data: MutableList<SoldProduct>,
    private val onItemClicked: (SoldProduct) -> Unit
) : RecyclerView.Adapter<BillDetailsAdapter.SDHolder>() {
    val catTrans = GetCategoryLocalName()

    class SDHolder(val binding: SoldItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SDHolder {
        val binding = SoldItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SDHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SDHolder, position: Int) {
        val item = data[position]
        val context = holder.binding.root.context

        holder.binding.pos.text = (position + 1).toString()
        holder.binding.brandName.text = "${item.name} (${catTrans(item.type)})"
        holder.binding.priceUnit.text = "$${item.sellingPrice} for each"
        holder.binding.count.text = "x${item.quantity}"
        holder.binding.total.text = "${item.sellingPrice * item.quantity} $"

        // Click only if it's a sold item
        holder.binding.root.setOnLongClickListener {
            onItemClicked(item)
            true
        }

    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SoldProduct>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
