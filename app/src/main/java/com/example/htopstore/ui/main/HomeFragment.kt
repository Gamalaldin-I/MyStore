package com.example.htopstore.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.model.GridItem
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.DateHelper
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentHomeBinding
import com.example.htopstore.ui.adding.AddProductActivity
import com.example.htopstore.ui.analysis.AnalysisActivity
import com.example.htopstore.ui.archive.ArchiveActivity
import com.example.htopstore.ui.dayDetails.DayDetailsActivity
import com.example.htopstore.ui.days.DaysActivity
import com.example.htopstore.ui.expenses.ExpensesActivity
import com.example.htopstore.ui.genCode.GenCodeActivity
import com.example.htopstore.ui.notifications.NotificationsActivity
import com.example.htopstore.ui.pendingSell.PendingSellActionsActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.ui.profile.ProfileActivity
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.ui.staff.StaffActivity
import com.example.htopstore.util.adapters.GridAdapter
import com.example.htopstore.util.adapters.LowStockAdapter
import com.example.htopstore.util.adapters.Top5Adapter
import com.example.htopstore.util.helper.Animator.animateGridItem
import com.example.htopstore.util.helper.PermissionHelper
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
        handleMainMenu()

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
            startActivity(Intent(requireContext(), AnalysisActivity::class.java))
        }
    }

    private fun goTo(button: View, onClick: () -> Unit) {
        button.animateGridItem {
            onClick()
        }
    }

    private fun handleMainMenu() {
        val isAdmin = PermissionHelper.isAdmin(viewModel.r)
        val spanCount = if (isAdmin) 3 else 2

        val allItems = listOf(
            GridItem(
                id = "scan",
                icon = R.drawable.grid_scan,
                title = getString(R.string.scancode),
            ) {
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            },
            GridItem(
                id = "expenses",
                icon = R.drawable.grid_outcome,
                title = getString(R.string.add_outcome),
            ) {
                startActivity(Intent(requireContext(), ExpensesActivity::class.java))
            },
            GridItem(
                id = "sales",
                icon = R.drawable.nav_report,
                title = getString(R.string.analysis),
            ) {
                startActivity(Intent(requireContext(), AnalysisActivity::class.java))
            },
            GridItem(
                id = "bills",
                icon = R.drawable.grid_bills,
                title = getString(R.string.nota),
            ) {
                startActivity(Intent(requireContext(), DaysActivity::class.java))
            },
            GridItem(
                id = "add",
                icon = R.drawable.grid_add,
                title = getString(R.string.add_new),
            ) {
                startActivity(Intent(requireContext(), AddProductActivity::class.java))
            },
            GridItem(
                id = "generate",
                icon = R.drawable.grid_code,
                title = getString(R.string.generateqrcodes),
            ) {
                startActivity(Intent(requireContext(), GenCodeActivity::class.java))
            }
        )
        val displayItems = if (isAdmin) allItems else allItems.take(4)

        binding.grid.gridRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = GridAdapter(displayItems)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(5, 5, 5, 5) // margins
                }
            })
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
            binding.lowStockCount.text = "${lowStockItems.size.ae()} ${getString(R.string.items)}"

            // Show/hide low stock card based on items
            binding.lowStockCard.visibility = if (lowStockItems.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        viewModel.profit.observe(viewLifecycleOwner) {
            val profit = it ?: 0.0
            binding.today.profit.text = profit.ae()
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { it ->
            val expenses = it ?: 0.0
            binding.today.expenses.text = expenses.ae()
        }

        viewModel.totalSales.observe(viewLifecycleOwner) {
            val sales = it ?: 0.0
            binding.today.salesValue.text = sales.ae()
        }
    }

    private fun goToProductDetails(productId: String) {
        if(PermissionHelper.canViewProduct(viewModel.r)){
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)}
    }

    private fun goToDayDetails() {
        val day = DateHelper.getCurrentDate()
        val intent = Intent(requireContext(), DayDetailsActivity::class.java)
        intent.putExtra("day", day)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        val staffItem = menu.findItem(R.id.staff)
        val archiveItem = menu.findItem(R.id.archive)
        if(!PermissionHelper.isAdmin(viewModel.r)){
            staffItem.isVisible = false
            archiveItem.isVisible = false
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.profile -> {
                startActivity(Intent(requireContext(), ProfileActivity::class.java))
                true
            }

            R.id.archive -> {
                startActivity(Intent(requireContext(), ArchiveActivity::class.java))
                    true
            }

            R.id.staff -> {
                startActivity(Intent(requireContext(), StaffActivity::class.java))
                true
            }
            R.id.pending -> {
                startActivity(Intent(requireContext(), PendingSellActionsActivity::class.java))
                true
            }
            R.id.notification->{
                startActivity(Intent(requireContext(), NotificationsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}