package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.domain.model.Product
import com.example.htopstore.R
import com.example.htopstore.databinding.ProductSelectionItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrSelectAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<QrSelectAdapter.ViewHolder>() {

    private var allProducts = listOf<Product>()
    private val selectedIds = mutableSetOf<String>()

    // Use AsyncListDiffer for background thread diffing
    private val differ = AsyncListDiffer(this, ProductDiffCallback())

    private val adapterScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var filterJob: Job? = null

    inner class ViewHolder(
        internal val binding: ProductSelectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            binding.apply {
                productName.text = product.name
                productCategory.text = product.category
                productId.text = "ID: ${product.id}"
                checkbox.isChecked = selectedIds.contains(product.id)

                // Optimized Glide loading with caching
                Glide.with(productImg)
                    .load(product.productImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_camera)
                    .error(R.drawable.ic_camera)
                    .centerCrop()
                    .dontAnimate() // Disable animations for better performance
                    .into(productImg)

                // Single click listener for better performance
                root.setOnClickListener { toggleSelection(product) }
                checkbox.setOnClickListener { toggleSelection(product) }
            }
        }

        private fun toggleSelection(product: Product) {
            val isSelected = selectedIds.contains(product.id)
            if (isSelected) {
                selectedIds.remove(product.id)
            } else {
                selectedIds.add(product.id)
            }

            // Update only the checkbox without rebinding
            binding.checkbox.isChecked = !isSelected
            onSelectionChanged(selectedIds.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductSelectionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size

    // Optimize with stable IDs
    override fun getItemId(position: Int): Long {
        return differ.currentList[position].id.hashCode().toLong()
    }

    init {
        setHasStableIds(true)
    }

    fun submitProducts(products: List<Product>) {
        allProducts = products
        selectedIds.clear()
        differ.submitList(products)
        onSelectionChanged(0)
    }

    fun filter(query: String) {
        // Cancel previous filter job if still running
        filterJob?.cancel()

        if (query.isBlank()) {
            differ.submitList(allProducts)
            return
        }

        // Run filtering on background thread
        filterJob = adapterScope.launch {
            val filtered = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.id.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }

            // Submit result on main thread
            withContext(Dispatchers.Main) {
                differ.submitList(filtered)
            }
        }
    }

    fun selectAll() {
        selectedIds.clear()
        selectedIds.addAll(differ.currentList.map { it.id })
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION_CHANGED)
        onSelectionChanged(selectedIds.size)
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION_CHANGED)
        onSelectionChanged(0)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Partial update for selection only
            val product = differ.currentList[position]
            holder.binding.checkbox.isChecked = selectedIds.contains(product.id)
        }
    }

    fun getSelectedProducts(): List<Product> {
        return allProducts.filter { selectedIds.contains(it.id) }
    }

    // Clean up coroutines
    fun onDestroy() {
        adapterScope.cancel()
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val PAYLOAD_SELECTION_CHANGED = "selection_changed"
    }
}