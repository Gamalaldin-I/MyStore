package com.example.htopstore.util.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.htopstore.databinding.CategoryItemBinding

class CategoriesAdapter(private val data: MutableList<String>,private val onDelete:(string:String,
                                                                                  onDeleteView:()->Unit)->Unit
) :
    RecyclerView.Adapter<CategoriesAdapter.CatHolder>() {

    // Create ViewHolder class
    class CatHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: CatHolder, position: Int) {
        holder.binding.name.text = data[position]
        holder.binding.deleteBtn.setOnClickListener {
            onDelete(data[position]){
                data.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, data.size)
            }
        }

    }
    fun insertNew(cat:String){
        data.add(cat)
        notifyItemInserted(data.size - 1)
        notifyItemRangeChanged(data.size - 1, data.size)
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}