package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.remoteModels.Invite
import com.example.htopstore.databinding.InboxInviteCardBinding

class InboxAdapter(private val data: MutableList<Invite>,
    private val onAcceptListener:(code:String,invite:Invite)->Unit,
    private val onRejectListener:(invite:Invite,position:Int)->Unit) :
    RecyclerView.Adapter<InboxAdapter.InHolder>() {

    // Create ViewHolder class
    class InHolder(val binding: InboxInviteCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InHolder {
        val binding =
            InboxInviteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: InHolder, position: Int) {
        val item = data[position]
        holder.binding.apply {
            storeName.text = item.storeName
            createdAt.text = item.createdAt
            acceptBtn.setOnClickListener {
                if(codeEt.text.toString().isEmpty()){
                    code.error = "Enter code"
                }else{
                    code.error = null
                    onAcceptListener(codeEt.text.toString().trim(),item)
                }
            }
            rejectBtn.setOnClickListener {
                onRejectListener(item,position)
            }
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: List<Invite>){
        this.data.clear()
        this.data.addAll(newList)
        notifyDataSetChanged()
    }
    fun deleteItem(pos:Int){
        data.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos,data.size)
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}