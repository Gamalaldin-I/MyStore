package com.example.htopstore.ui.main

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.Product
import com.example.domain.useCase.localize.GetCategoryLocalName
import com.example.htopstore.databinding.FragmentStoreBinding
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.StockRecycler
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
@AndroidEntryPoint
class StockFragment : Fragment() {
    val vm: MainViewModel by activityViewModels()
    private val getCatLocalName = GetCategoryLocalName()
    private lateinit var binding: FragmentStoreBinding
    private val adapter by lazy {
        StockRecycler {
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
        localLanguage = Locale.getDefault().language
        all = if (localLanguage == "ar") "الكل" else "All"

        setupRecycler()
        observeProducts()

        return binding.root
    }

    private fun setupRecycler() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = this@StockFragment.adapter
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
    private fun observeProducts() {
        vm.products.observe(viewLifecycleOwner){
            val distinctCategories = it.map { getCatLocalName(it.category) }.distinct()
            allProducts = it
            categories = distinctCategories
            adapter.submitList(allProducts)
            setupChips()
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
                allProducts.filter{getCatLocalName(it.category) == selectedCategory }
            }

            adapter.submitList(filtered)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.getStockProducts()
    }
}