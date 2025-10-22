package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.category.UserRoles
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.htopstore.databinding.StaffMemberCardBinding

class EmployeeAdapter(private val data: MutableList<StoreEmployee>) :
    RecyclerView.Adapter<EmployeeAdapter.EHolder>() {

    // Create ViewHolder class
    class EHolder(val binding: StaffMemberCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EHolder {
        val binding =
            StaffMemberCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: EHolder, position: Int) {
        val item = data[position]
        holder.binding.apply {
            employeeName.text = item.name
            employeeEmail.text = item.email
            employeeRole.text = UserRoles.entries.find { it.role == item.role }?.roleName ?: "Unknown"
        }

    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<StoreEmployee>){
        this.data.clear()
        this.data.addAll(newList)
        notifyDataSetChanged()
    }
}