package com.example.htopstore.ui.stock

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.databinding.FragmentStoreBinding
import com.example.htopstore.domain.useCase.CategoryLocalManager
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.StockRecycler
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class StoreFragment : Fragment() {

    private lateinit var binding: FragmentStoreBinding
    private lateinit var productRepo: ProductRepoImp
    private val adapter by lazy { StockRecycler{
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", it.id)
        startActivity(intent)
    }
    }
    private lateinit var localLanguage: String
    private lateinit var all: String

    private var allProducts = emptyList<Product>()
    private var categories = emptyList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStoreBinding.inflate(inflater, container, false)
        productRepo = ProductRepoImp(requireContext())
        localLanguage = Locale.getDefault().language
        all = if (localLanguage == "ar") "الكل" else "All"

        setupRecycler()
        loadProducts()

        return binding.root
    }

    private fun setupRecycler() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = this@StoreFragment.adapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val spacing = 8
                    outRect.set(spacing, spacing, spacing, spacing)
                }
            })
        }
    }
    private fun loadProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val products = productRepo.getProductsAvailable()
            val distinctCategories = products.map { CategoryLocalManager.getCategoryNameLocal(it.category) }.distinct()

            withContext(Dispatchers.Main) {
                allProducts = products
                categories = distinctCategories
                adapter.submitList(allProducts)
                setupChips()
            }
        }
    }

    private fun setupChips() {
        binding.chipGroup.removeAllViews()

        // Chip "All"
        val chipAll = Chip(requireContext()).apply {
            text = all
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
        }
        binding.chipGroup.addView(chipAll)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                id = View.generateViewId()
            }
            binding.chipGroup.addView(chip)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                adapter.submitList(allProducts)
                return@setOnCheckedStateChangeListener
            }

            val selectedChip = group.findViewById<Chip>(checkedIds.first())
            val selectedCategory = selectedChip.text.toString()

            val filtered = if (selectedCategory == all) {
                allProducts
            } else {
                allProducts.filter { CategoryLocalManager.getCategoryNameLocal(it.category) == selectedCategory }
            }

            adapter.submitList(filtered)
        }
    }
}
