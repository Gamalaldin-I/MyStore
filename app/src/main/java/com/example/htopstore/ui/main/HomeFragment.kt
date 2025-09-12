package com.example.htopstore.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.htopstore.databinding.FragmentMainBinding
import com.example.htopstore.ui.AddProductActivity
import com.example.htopstore.ui.ReturnsActivity
import com.example.htopstore.ui.bills.BillsActivity
import com.example.htopstore.ui.expenses.ExpensesActivity
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.ui.qrGen.QRCodeGenActivity
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.util.Animator.animateGridItem
import com.example.htopstore.util.adapters.LowStockAdapter
import com.example.htopstore.util.adapters.Top5Adapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var viewPager:ViewPager2
    private lateinit var top5Adapter: Top5Adapter
    private lateinit var lowStockAdapter: LowStockAdapter
    private val viewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        getTop5InSales()
        getLowStock()
        getTodayBrief()

    }

    override fun onResume() {
        super.onResume()
        initialize()
    }

    private fun setupTop5Adapter() {
        top5Adapter = Top5Adapter(mutableListOf()){
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
    }

    private fun setupLowStockAdapter() {
        lowStockAdapter = LowStockAdapter(mutableListOf()){
            goToProductDetails(it.id)
        }
        // Make it horizontal
        binding.lowStock.adapter.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.lowStock.adapter.adapter = lowStockAdapter
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
        viewModel.getTop5InSales()
        viewModel.top5.observe(viewLifecycleOwner){
            top5Adapter.updateData(it)
        }
    }

    private fun getLowStock(){
        viewModel.getLowStock()
        viewModel.lowStock.observe(viewLifecycleOwner){
            lowStockAdapter.updateData(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTodayBrief(){
        viewModel.getTodayBrief()
        viewModel.todayBrief.observe(viewLifecycleOwner){
            binding.today.salesValue.text = "${it.income} $"
            binding.today.expenses.text = "${it.expenses} $"
            binding.today.profit.text = "${it.profit} $"
            }
        }

    private fun goToProductDetails(productId: String) {
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", productId)
        startActivity(intent)
    }

}
