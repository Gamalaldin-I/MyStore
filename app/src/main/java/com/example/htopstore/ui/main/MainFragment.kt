package com.example.htopstore.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.home.HomeRepoImp
import com.example.htopstore.databinding.FragmentMainBinding
import com.example.htopstore.ui.AddProductActivity
import com.example.htopstore.ui.ReturnsActivity
import com.example.htopstore.ui.bills.BillsActivity
import com.example.htopstore.ui.expenses.ExpensesActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.ui.qrGen.QRCodeGenActivity
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.util.adapters.Top5Adapter
import com.example.htopstore.util.Animator.animateGridItem
import com.example.htopstore.util.DateHelper
import com.example.htopstore.util.adapters.LowStockAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Suppress("DEPRECATION")
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var homeRepo: HomeRepoImp
    private var top5 = mutableListOf<Product>()
    private var lowStock = mutableListOf<Product>()
    private lateinit var viewPager:ViewPager2
    private lateinit var top5Adapter: Top5Adapter
    private lateinit var lowStockAdapter: LowStockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeRepo = HomeRepoImp(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        setControllers()


        return binding.root
    }
    fun initialize(){
        setupTop5Adapter()
        setupLowStockAdapter()
        getTodayDetails()
    }

    override fun onResume() {
        super.onResume()
        initialize()
    }

    private fun setupTop5Adapter() {
        top5Adapter = Top5Adapter(top5){
            goToProductDetails(it.id)
        }
        // make the adapter horizontal
        // Make it horizontal
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
        //viewPager.currentItem = 1

        viewPager.adapter = top5Adapter


        getTop5InSales()
    }

    private fun setupLowStockAdapter() {
        lowStockAdapter = LowStockAdapter(lowStock){
            goToProductDetails(it.id)
        }
        // Make it horizontal
        binding.lowStock.adapter.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.lowStock.adapter.adapter = lowStockAdapter
        getLowStock()
    }


    private fun setControllers(){
        handelMainMenu()
    }

    fun goTo(button: View, onClick : () -> Unit){
            button.animateGridItem {
                onClick()
            }
        }
    private fun handelMainMenu(){
        val add = binding.grid.addProducts
        val bills = binding.grid.bills
        val generate = binding.grid.generate
        val expenses = binding.grid.expenses
        val sales = binding.grid.sales
        val scan = binding.grid.scan

        add.setOnClickListener {
            goTo(add){
                startActivity(Intent(requireContext(), AddProductActivity::class.java))}
        }
        bills.setOnClickListener {
            goTo(bills){startActivity(Intent(requireContext(), BillsActivity::class.java))
            }}
        generate.setOnClickListener {
            goTo(generate) {startActivity(Intent(requireContext(), QRCodeGenActivity::class.java))
            }}
        expenses.setOnClickListener {
            goTo(expenses){ startActivity(Intent(requireContext(), ExpensesActivity::class.java))
            }}
        sales.setOnClickListener {
            goTo(sales){
                startActivity(Intent(requireContext(), ReturnsActivity::class.java))}
        }
        scan.setOnClickListener {
            goTo(scan) {
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            }
        }
    }


    private fun getTop5InSales(){
        lifecycleScope.launch(Dispatchers.IO) {
            top5 = homeRepo.getTop5InSales() as MutableList<Product>
            withContext(Dispatchers.Main) {
                top5Adapter.updateData(top5)
            }
        }
    }
    private fun getLowStock(){
        lifecycleScope.launch(Dispatchers.IO) {
            lowStock = homeRepo.getLowStock() as MutableList<Product>
            withContext(Dispatchers.Main) {
                lowStockAdapter.updateData(lowStock)
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getTodayDetails(){
        lifecycleScope.launch(Dispatchers.IO) {
            val toDay = DateHelper.getCurrentDate()
            val profit = homeRepo.getProfitToday(toDay)?:0.0
            val income = homeRepo.getIncomeToday(toDay)?:0.0
            val expenses = homeRepo.getExpensesToday(toDay)?:0.0
            withContext(Dispatchers.Main) {
                binding.today.salesValue.text = "$income $"
                binding.today.expenses.text = "$expenses $"
                binding.today.profit.text = "${profit-expenses} $"
            }

        }

    }
    private fun goToProductDetails(productId: String) {
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", productId)
        startActivity(intent)
    }

}
