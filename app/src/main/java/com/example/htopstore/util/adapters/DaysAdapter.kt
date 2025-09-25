package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htopstore.databinding.DayItemBinding

class DaysAdapter(private val data: ArrayList<String>,private val onClick:(s:String)-> Unit) :
    RecyclerView.Adapter<DaysAdapter.DHolder>() {

    // Create ViewHolder class
    class DHolder(val binding: DayItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DHolder {
        val binding = DayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: DHolder, position: Int) {
        val item = data[position]
        holder.binding.date.text = item
        holder.binding.root.setOnClickListener {
            // Handle item click here
            onClick(item)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<String>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}