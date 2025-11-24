package com.example.htopstore.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.util.DateHelper
import com.example.htopstore.databinding.FragmentHomeBinding
import com.example.htopstore.ui.adding.AddProductActivity
import com.example.htopstore.ui.analysis.AnalysisActivity
import com.example.htopstore.ui.dayDetails.DayDetailsActivity
import com.example.htopstore.ui.days.DaysActivity
import com.example.htopstore.ui.expenses.ExpensesActivity
import com.example.htopstore.ui.genCode.GenCodeActivity
import com.example.htopstore.ui.pendingSell.PendingSellActionsActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.ui.staff.StaffActivity
import com.example.htopstore.util.adapters.LowStockAdapter
import com.example.htopstore.util.adapters.Top5Adapter
import com.example.htopstore.util.helper.Animator.animateGridItem
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var top5Adapter: Top5Adapter
    private lateinit var lowStockAdapter: LowStockAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initializeViews()
        setControllers()
        setupTop5Adapter()
        setupLowStockAdapter()
        observe()

        return binding.root
    }

    private fun initializeViews() {
        // Set current date in header
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        binding.todayDate.text = dateFormat.format(Date())
    }

    @SuppressLint("SetTextI18n")
    private fun setupTop5Adapter() {
        binding.top5.title.visibility = View.GONE
        top5Adapter = Top5Adapter(mutableListOf()) {
            goToProductDetails(it.id)
        }

        // Setup ViewPager2 for Top 5
        viewPager = binding.top5.viewPager
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.offscreenPageLimit = 3
        viewPager.clipToPadding = false
        viewPager.clipChildren = false

        (viewPager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        viewPager.setPageTransformer(CompositePageTransformer().apply {
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.75f + r * 0.25f
                page.scaleX = 0.75f + r * 0.25f
            }
        })

        viewPager.adapter = top5Adapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupLowStockAdapter() {
        binding.lowStock.title.visibility = View.GONE
        lowStockAdapter = LowStockAdapter(mutableListOf()) {
            goToProductDetails(it.id)
        }

        // Make it horizontal
        binding.lowStock.adapter.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.lowStock.adapter.adapter = lowStockAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setControllers() {
        handleMainMenu()
        handleTodaySummaryClicks()
        handleTopSalesButton()
    }

    private fun handleTodaySummaryClicks() {
        // Make today's summary items clickable
        binding.today.expenses.setOnClickListener {
            goTo(binding.today.expenses) {
                goToDayDetails()
            }
        }

        binding.today.salesValue.setOnClickListener {
            goTo(binding.today.salesValue) {
                goToDayDetails()
            }
        }

        binding.today.profit.setOnClickListener {
            goTo(binding.today.profit) {
                goToDayDetails()
            }
        }
    }

    private fun handleTopSalesButton() {
        // Handle View All button for Top 5 Sales
        binding.viewAllSales.setOnClickListener {
            // Navigate to full sales list or analytics
            startActivity(Intent(requireContext(), PendingSellActionsActivity::class.java))
        }
    }

    private fun goTo(button: View, onClick: () -> Unit) {
        button.animateGridItem {
            onClick()
        }
    }

    private fun handleMainMenu() {
        val add = binding.grid.addProducts
        val bills = binding.grid.bills
        val generate = binding.grid.generateQrs
        val expenses = binding.grid.expenses
        val sales = binding.grid.sales
        val scan = binding.grid.scan
        val staff = binding.staff

        add.setOnClickListener {
            goTo(add) {
                startActivity(Intent(requireContext(), AddProductActivity::class.java))
            }
        }

        bills.setOnClickListener {
            goTo(bills) {
                startActivity(Intent(requireContext(), DaysActivity::class.java))
            }
        }

        generate.setOnClickListener {
            goTo(generate) {
                startActivity(Intent(requireContext(), GenCodeActivity::class.java))
            }
        }

        expenses.setOnClickListener {
            goTo(expenses) {
                startActivity(Intent(requireContext(), ExpensesActivity::class.java))
            }
        }

        sales.setOnClickListener {
            goTo(sales) {
                startActivity(Intent(requireContext(), AnalysisActivity::class.java))
            }
        }

        scan.setOnClickListener {
            goTo(scan) {
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            }
        }

        staff.setOnClickListener {
            goTo(staff) {
                startActivity(Intent(requireContext(), StaffActivity::class.java))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        viewModel.top5.observe(viewLifecycleOwner) {
            top5Adapter.updateData(it)
        }

        viewModel.lowStock.observe(viewLifecycleOwner) { lowStockItems ->
            lowStockAdapter.updateData(lowStockItems)
            // Update low stock count chip
            binding.lowStockCount.text = "${lowStockItems.size} items"

            // Show/hide low stock card based on items
            binding.lowStockCard.visibility = if (lowStockItems.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        viewModel.profit.observe(viewLifecycleOwner) {
            val profit = it ?: 0.0
            binding.today.profit.text = profit.toString()
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { it ->
            val expenses = it ?: 0.0
            binding.today.expenses.text = expenses.toString()
        }

        viewModel.totalSales.observe(viewLifecycleOwner) {
            val sales = it ?: 0.0
            binding.today.salesValue.text = sales.toString()
        }
    }

    private fun goToProductDetails(productId: String) {
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", productId)
        startActivity(intent)
    }

    private fun goToDayDetails() {
        val day = DateHelper.getCurrentDate()
        val intent = Intent(requireContext(), DayDetailsActivity::class.java)
        intent.putExtra("day", day)
        startActivity(intent)
    }
}