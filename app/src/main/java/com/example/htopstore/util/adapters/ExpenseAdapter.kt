package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.Expense
import com.example.htopstore.databinding.ExpenseItemBinding

class ExpenseAdapter(private val data: ArrayList<Expense>, private val onClick: (expense: Expense)-> Unit) :
    RecyclerView.Adapter<ExpenseAdapter.EHolder>() {

    // Create ViewHolder class
    class EHolder(val binding: ExpenseItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EHolder(binding)
    }

    // Bind data to the ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EHolder, position: Int) {
        val item = data[position]
        holder.binding.date.text= item.date
        holder.binding.method.text = item.paymentMethod
        holder.binding.total.text = "${item.amount}$"
        holder.binding.time.text = item.time
        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Expense>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
    fun addExpense(expense: Expense) {
        data.add(expense)
        notifyItemInserted(data.size - 1)
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}