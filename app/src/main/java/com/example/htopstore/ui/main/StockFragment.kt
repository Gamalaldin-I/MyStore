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
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentStoreBinding
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.StockRecycler
import com.example.htopstore.util.helper.Animator.animateSelectedChip
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StockFragment : Fragment() {

    private var checkedId = -1
    private lateinit var binding: FragmentStoreBinding
    private val vm: MainViewModel by activityViewModels()
    private val getCatLocalName = GetCategoryLocalName()
    private val adapter by lazy {
        StockRecycler { product ->
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }
    }

    private lateinit var localLanguage: String
    private lateinit var allText: String
    private lateinit var stockText: String
    private lateinit var categoriesText: String
    private lateinit var managingInventoryText: String
    private lateinit var noItemsText: String
    private lateinit var addItemsText: String

    private var allProducts = emptyList<Product>()
    private var categories = emptyList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreBinding.inflate(inflater, container, false)
        initLocalText()
        setupUI()
        setupRecycler()
        setupSwipeRefresh()
        observeProducts()
        return binding.root
    }

    // --------------------- Initialization ---------------------
    private fun initLocalText() {
        localLanguage = Locale.getDefault().language

        if (localLanguage == "ar") {
            allText = "الكل"
            stockText = "المخزون"
            categoriesText = "الفئات"
            managingInventoryText = "إدارة مخزونك"
            noItemsText = "لا توجد عناصر في المخزون"
            addItemsText = "أضف عناصر للبدء"
        } else {
            allText = "All"
            stockText = "Stock"
            categoriesText = "Categories"
            managingInventoryText = "Managing your inventory"
            noItemsText = "No items in stock"
            addItemsText = "Add items to get started"
        }
    }

    private fun setupUI() {
        // Set header text
        binding.headerCard.findViewById<android.widget.TextView>(R.id.tvStockCount)?.text = managingInventoryText

        // Set categories label
        binding.tvCategoriesLabel.text = categoriesText

        // Set empty state text if exists
        binding.emptyHint.text = noItemsText
        binding.tvEmptySubtitle.text = addItemsText

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

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.primary_navy_blue,
                R.color.primary_accent_blue
            )

            setOnRefreshListener {
                refreshData()
            }
        }
    }

    // --------------------- Observers ---------------------
    private fun observeProducts() {
        vm.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            categories = products.map { getCatLocalName(it.category) }.distinct()

            updateUI()
            setupChips()

            // Stop refresh animation if running
            binding.swipeRefresh.isRefreshing = false
        }
    }

    // --------------------- UI Updates ---------------------
    private fun updateUI() {
        if (allProducts.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            adapter.submitList(allProducts)
        }

        // Update stock count in header
        val countText = if (localLanguage == "ar") {
            "$managingInventoryText • ${allProducts.size} منتج"
        } else {
            "$managingInventoryText • ${allProducts.size} items"
        }
        binding.headerCard.findViewById<android.widget.TextView>(R.id.tvStockCount)?.text = countText
    }

    private fun refreshData() {
        // Trigger data refresh from ViewModel
        vm.startListenForProducts()

        // If your ViewModel doesn't have a refresh method, you can manually stop the animation
        // after a delay or when the observer updates
        binding.root.postDelayed({
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 2000)
    }

    // --------------------- Chip Logic ---------------------
    private fun setupChips() {
        binding.chipGroup.removeAllViews()

        // Add "All" chip
        addChip(allText, isChecked = true)

        // Add category chips
        categories.forEach { category ->
            addChip(category)
        }

        // Handle chip selection
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            handleChipSelection(group, checkedIds)
        }
    }

    private fun addChip(text: String, isChecked: Boolean = false) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            this.isCheckable = true
            this.isChecked = isChecked
            this.id = View.generateViewId()

            chipBackgroundColor = if (isChecked) {
                requireContext().getColorStateList(R.color.neutral_200)
            } else {
                requireContext().getColorStateList(R.color.white)
            }
        }
        binding.chipGroup.addView(chip)
    }

    private fun handleChipSelection(group: ViewGroup, checkedIds: List<Int>) {
        if (checkedIds.isEmpty()) {
            adapter.submitList(allProducts)
            updateEmptyState(allProducts)
            checkedId = -1
            return
        }

        val newCheckedId = checkedIds.first()
        if (checkedId != newCheckedId) {
            // Deselect previous chip
            if (checkedId != -1) {
                val previousChip = group.findViewById<Chip>(checkedId)
                previousChip?.animateSelectedChip(false)
            }

            // Select new chip
            checkedId = newCheckedId
            val selectedChip = group.findViewById<Chip>(checkedId)
            selectedChip?.animateSelectedChip(true)

            // Filter products
            val selectedCategory = selectedChip?.text.toString()
            val filtered = if (selectedCategory == allText) {
                allProducts
            } else {
                allProducts.filter { getCatLocalName(it.category) == selectedCategory }
            }

            adapter.submitList(filtered)
            updateEmptyState(filtered)
        }
    }

    private fun updateEmptyState(products: List<Product>) {
        if (products.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.swipeRefresh.setOnRefreshListener(null)
    }
}