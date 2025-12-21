package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.util.NotificationTimeUtils
import com.example.htopstore.R
import com.example.htopstore.databinding.InboxInviteCardBinding

class InboxAdapter(private val data: MutableList<Invitation>,
                   private val onAcceptListener:(code:String,invite:Invitation)->Unit,
                   private val onRejectListener:(invite:Invitation, position:Int)->Unit) :
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
            createdAt.text = NotificationTimeUtils.getRelativeTime(item.createdAt!!)
            Glide.with(inviteIcon.context)
                .load(item.storeIcon)
                .error(R.drawable.oc_invite)
                .placeholder(R.drawable.oc_invite)
                .into(inviteIcon)
            acceptBtn.setOnClickListener {
                if(codeEt.text.toString().isEmpty()){
                    codeInput.error = "Enter code"
                }else{
                    codeInput.error = null
                    onAcceptListener(codeEt.text.toString().trim(),item)
                }
            }
            rejectBtn.setOnClickListener {
                onRejectListener(item,position)
            }
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: List<Invitation>){
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