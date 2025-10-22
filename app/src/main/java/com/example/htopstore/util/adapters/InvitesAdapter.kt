package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.remoteModels.Invite
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.htopstore.R
import com.example.htopstore.databinding.InviteCardBinding

class InvitesAdapter(private val data: MutableList<Invite>,
                     private val onDelete:(invite: Invite)->Unit,
                     private val onShare:(invite: Invite)->Unit) :
    RecyclerView.Adapter<InvitesAdapter.InviteHolder>() {

    // Create ViewHolder class
    class InviteHolder(val binding: InviteCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteHolder {
        val binding = InviteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InviteHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: InviteHolder, position: Int) {
        val item = data[position]
        val context = holder.binding.root.context
        holder.binding.apply {
            email.text = item.email
            status.text = item.status
            when(status.text){
                STATUS_PENDING -> status.setTextColor(
                    ContextCompat.getColor(context, R.color.size_small)
                )
                STATUS_ACCEPTED -> status.setTextColor(
                    ContextCompat.getColor(context, R.color.action_primary)
                )
                else -> status.setTextColor(
                    ContextCompat.getColor(context, R.color.alert_critical)
                )
            }

            code.text = item.code
            createdAt.text = item.createdAt.toString()
            shareBtn.setOnClickListener {
                onShare(item)
            }
            deleteBtn.setOnClickListener {
                onDelete(item)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: MutableList<Invite>){
        this.data.clear()
        this.data.addAll(newList)
        notifyDataSetChanged()
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}