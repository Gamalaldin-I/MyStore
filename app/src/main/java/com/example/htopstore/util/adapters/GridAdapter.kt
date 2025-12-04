package com.example.htopstore.util.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.GridItem
import com.example.htopstore.databinding.MainGridItemBinding
import com.example.htopstore.util.helper.Animator.animateGridItem

class GridAdapter(private val items: List<GridItem>) :
    RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    inner class GridViewHolder(private val binding: MainGridItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: GridItem) {
            binding.itemIcon.setImageResource(item.icon)
            binding.itemTitle.text = item.title
            binding.root.setOnClickListener {
                goTo(binding.root){item.onClick()}
        }
        }
    }
    private fun goTo(button: View, onClick: () -> Unit) {
        button.animateGridItem {
            onClick()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = MainGridItemBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}