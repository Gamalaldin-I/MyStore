package com.example.htopstore.ui.main

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.Product
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
    private val adapter by lazy {
        StockRecycler { product ->
            onProductClick(product)
        }
    }

    private lateinit var localLanguage: String
    private lateinit var allText: String
    private lateinit var stockText: String
    private lateinit var categoriesText: String
    private lateinit var managingInventoryText: String
    private lateinit var noItemsText: String
    private lateinit var addItemsText: String
    private lateinit var searchHintText: String
    private lateinit var noResultsText: String
    private lateinit var showingText: String
    private lateinit var productsText: String

    private var allProducts = emptyList<Product>()
    private var categories = emptyList<String>()
    private var currentSearchQuery = ""
    private var currentFilteredProducts = emptyList<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreBinding.inflate(inflater, container, false)
        initLocalText()
        setupUI()
        setupRecycler()
        setupSearchBar()
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
            noItemsText = "لا توجد عناصر"
            addItemsText = "أضف عناصر للبدء"
            searchHintText = "ابحث عن المنتجات..."
            noResultsText = "لم يتم العثور على نتائج"
            showingText = "عرض"
            productsText = "منتج"
        } else {
            allText = "All"
            stockText = "Stock"
            categoriesText = "Categories"
            managingInventoryText = "Managing your inventory"
            noItemsText = "No items found"
            addItemsText = "Try adjusting your search or filters"
            searchHintText = "Search products..."
            noResultsText = "No results found"
            showingText = "Showing"
            productsText = "products"
        }
    }

    private fun setupUI() {
        // Set header text
        binding.tvStockCount.text = managingInventoryText

        // Set categories label
        binding.tvCategoriesLabel.text = categoriesText

        // Set empty state text
        binding.emptyHint.text = noItemsText
        binding.tvEmptySubtitle.text = addItemsText

        // Set search hint
        binding.searchEditText.hint = searchHintText
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

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""

                // Show/hide clear button
                binding.btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // Apply filters
                applyFiltersAndSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.clearFocus()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary_accent_blue),
                ContextCompat.getColor(requireContext(), R.color.action_primary)
            )

            setOnRefreshListener {
                refreshData()
            }
        }
    }

    // --------------------- Observers ---------------------
    private fun observeProducts() {
        vm.fetchProductsFromRemote()
        vm.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            categories = products.map {it.category}.distinct()

            setupChips()
            applyFiltersAndSearch()

            // Stop refresh animation if running
            binding.swipeRefresh.isRefreshing = false
        }
    }

    // --------------------- Filtering & Search ---------------------
    private fun applyFiltersAndSearch() {
        // Start with all products
        var filteredProducts = allProducts

        // Apply category filter
        val selectedChip = if (checkedId != -1) {
            binding.chipGroup.findViewById<Chip>(checkedId)
        } else {
            null
        }

        val selectedCategory = selectedChip?.text?.toString()
        if (selectedCategory != null && selectedCategory != allText) {
            filteredProducts = filteredProducts.filter {
                it.category == selectedCategory
            }
        }

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            filteredProducts = filteredProducts.filter { product ->
                product.name.contains(currentSearchQuery, ignoreCase = true) ||
                        product.id.contains(currentSearchQuery, ignoreCase = true) ||
                        product.category.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        currentFilteredProducts = filteredProducts
        updateUI(filteredProducts, selectedCategory)
    }

    // --------------------- UI Updates ---------------------
    private fun updateUI(products: List<Product>, activeFilter: String? = null) {
        // Update RecyclerView
        adapter.submitList(products)

        // Update empty state
        if (products.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.resultsCard.visibility = View.GONE

            // Update empty state message based on context
            if (currentSearchQuery.isNotEmpty()) {
                binding.emptyHint.text = noResultsText
                binding.tvEmptySubtitle.text = addItemsText
            } else if (allProducts.isEmpty()) {
                binding.emptyHint.text = noItemsText
                binding.tvEmptySubtitle.text = if (localLanguage == "ar") {
                    "أضف عناصر للبدء"
                } else {
                    "Add items to get started"
                }
            } else {
                binding.emptyHint.text = noResultsText
                binding.tvEmptySubtitle.text = addItemsText
            }
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.resultsCard.visibility = View.VISIBLE
        }

        // Update results count
        updateResultsCount(products.size)

        // Update stock count in header
        updateHeaderCount()

        // Update active filter chip
        if (activeFilter != null && activeFilter != allText) {
            binding.activeFilterChip.visibility = View.VISIBLE
            binding.activeFilterChip.text = activeFilter
        } else {
            binding.activeFilterChip.visibility = View.GONE
        }
    }

    private fun updateResultsCount(count: Int) {
        val countText = if (localLanguage == "ar") {
            "$showingText $count $productsText"
        } else {
            "$showingText $count $productsText"
        }
        binding.tvResultsCount.text = countText
    }

    private fun updateHeaderCount() {
        val countText = if (localLanguage == "ar") {
            "$managingInventoryText • ${allProducts.size} منتج"
        } else {
            "$managingInventoryText • ${allProducts.size} items"
        }
        binding.tvStockCount.text = countText
    }

    private fun refreshData() {
        vm.fetchProductsFromRemote()

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

            // Style the chip
            setChipBackgroundColorResource(
                if (isChecked) R.color.primary_accent_blue else R.color.neutral_100
            )
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isChecked) R.color.text_white else R.color.text_primary
                )
            )
        }
        binding.chipGroup.addView(chip)
    }

    private fun handleChipSelection(group: ViewGroup, checkedIds: List<Int>) {
        if (checkedIds.isEmpty()) {
            checkedId = -1
            applyFiltersAndSearch()
            return
        }

        val newCheckedId = checkedIds.first()
        if (checkedId != newCheckedId) {
            // Deselect previous chip
            if (checkedId != -1) {
                val previousChip = group.findViewById<Chip>(checkedId)
                previousChip?.apply {
                    setChipBackgroundColorResource(R.color.neutral_100)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    animateSelectedChip(false)
                }
            }

            // Select new chip
            checkedId = newCheckedId
            val selectedChip = group.findViewById<Chip>(checkedId)
            selectedChip?.apply {
                setChipBackgroundColorResource(R.color.primary_accent_blue)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                animateSelectedChip(true)
            }

            // Apply filters
            applyFiltersAndSearch()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.swipeRefresh.setOnRefreshListener(null)
        binding.searchEditText.removeTextChangedListener(null)
    }
    ///////////////////////////////////////////////////////////////
    //////////////////////Authorization///////////////////////////
    /////////////////////////////////////////////////////////////
    private fun onProductClick(product: Product) {
        if(true){
            //TODO:  add check for role first
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }
    }
}
