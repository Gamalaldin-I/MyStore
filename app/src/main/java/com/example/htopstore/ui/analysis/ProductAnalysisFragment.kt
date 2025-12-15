package com.example.htopstore.ui.analysis

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.model.CategorySales
import com.example.domain.util.DateHelper.DAY
import com.example.domain.util.DateHelper.MONTH
import com.example.domain.util.DateHelper.WEEK
import com.example.domain.util.DateHelper.YEAR
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentProductAnalysisBinding
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.Visualiser.drawPieChart
import com.example.htopstore.util.adapters.LowStockAdapter
import com.example.htopstore.util.adapters.MostProfitableAdapter
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class ProductAnalysisFragment : Fragment() {
    private lateinit var topProfitAdapter: MostProfitableAdapter
    private lateinit var haveNotSoldAdapter: LowStockAdapter
    private lateinit var viewPager: ViewPager2
     private lateinit var binding: FragmentProductAnalysisBinding
     private val vm: AnalysisViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
         binding = FragmentProductAnalysisBinding.inflate(inflater, container, false)
        val autoComplete: AutoCompleteTextView = binding.autoComplete

        val options = listOf(DAY,WEEK,MONTH,YEAR)
        autoComplete.setAdapter(AutoCompleteHelper.getDurationAdapter(requireContext()))

        autoComplete.setText(WEEK, false)
        vm.getProductsAnalysis(WEEK)
        setUpMostProfitAdapter()
        setupLowStockAdapter()
        vm.getHaveNotSoldProducts()
        vm.getTheHighestProfitProducts()

        autoComplete.setOnItemClickListener { parent, view, position, id ->
            val selected = options[position]
            // handle logic according to selected option
            vm.getProductsAnalysis(selected)

        }
         observeViewModel()

        return binding.root

    }



    @SuppressLint("SetTextI18n")
    private fun setUpMostProfitAdapter() {
        topProfitAdapter = MostProfitableAdapter(mutableListOf()) {
            goToProductDetails(it.id)
        }
        // make the adapter horizontal
        // Make it horizontal
        binding.top5.title.text = getString(R.string.mostProfitable)
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

        viewPager.adapter = topProfitAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupLowStockAdapter() {
        binding.haveNotSold.title.text = getString(R.string.haveNotSold)
        haveNotSoldAdapter = LowStockAdapter(mutableListOf(),false) {
            goToProductDetails(it.id)
        }
        // Make it horizontal
        binding.haveNotSold.adapter.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.haveNotSold.adapter.adapter = haveNotSoldAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel(){
        vm.sellingCategory.observe(viewLifecycleOwner){ it->
            if(it.isNullOrEmpty()){
                binding.charts.salesChartLo.visibility = View.GONE
            }
            else{
            binding.charts.salesChartLo.visibility = View.VISIBLE
            binding.charts.sellingCategoriesChart.data = null
            binding.charts.sellingCategoriesChart.invalidate()
            binding.charts.sellingCategoriesChart.clear()
            drawPieChart(binding.charts.sellingCategoriesChart,it.mapToListOfPieEntry(),"")
        }}
        vm.returningCategories.observe(viewLifecycleOwner){ it ->
            if(it.isNullOrEmpty()){
                binding.charts.returningChartLO.visibility = View.GONE
            }
            else{
            binding.charts.returningChartLO.visibility = View.VISIBLE
            binding.charts.returningCategories.data = null
            binding.charts.returningCategories.invalidate()
            binding.charts.returningCategories.clear()
            drawPieChart(binding.charts.returningCategories,it.mapToListOfPieEntry(),"")}
        }

        vm.haveNotSoldProducts.observe(viewLifecycleOwner){
            haveNotSoldAdapter.updateData(it)
        }
        vm.theHighestProfitProducts.observe(viewLifecycleOwner){
            topProfitAdapter.updateData(it)
        }
        vm.theLeastSellingCategory.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                binding.numbers.theLeastSellingCategory.text = "No data"
            }else binding.numbers.theLeastSellingCategory.text = it
        }
        vm.theMostSellingCategory.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                binding.numbers.mostSalesCategory.text = "No data"
            }else binding.numbers.mostSalesCategory.text = it
        }
    }

    private fun List<CategorySales>?.mapToListOfPieEntry(): List<PieEntry> {
        return this!!.map {
            val totalSold = abs( it.totalSold)
            PieEntry(totalSold.toFloat(), it.type)
        }
    }
    private fun goToProductDetails(productId: String) {
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", productId)
        startActivity(intent)
    }




}