package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htopstore.data.local.model.SellOp
import com.example.htopstore.databinding.BillItemBinding

class BillsAdapter(private var data: List<SellOp>, val onItemClicked: (sellOpID: String) -> Unit
) :
    RecyclerView.Adapter<BillsAdapter.SHolder>() {

    // Create ViewHolder class
    class SHolder(val binding: BillItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SHolder {
        val binding = BillItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SHolder(binding)
    }

    // Bind data to the ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SHolder, position: Int) {
        val currentItem = data[position]
        holder.binding.date.text = "Date: ${currentItem.date}"
        holder.binding.time.text = currentItem.time
        holder.binding.total.text = "${currentItem.totalCash} $"
        holder.binding.root.setOnClickListener {
            onItemClicked(currentItem.saleId)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
    fun getSumOfBills(): Double {
        var sum = 0.0
        for (bill in data) {
            sum += bill.totalCash
        }
        return sum
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SellOp>) {
        this.data = newList
        notifyDataSetChanged()
    }

}