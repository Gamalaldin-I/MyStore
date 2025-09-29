package com.example.htopstore.ui.analysis

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityAnalysisBinding
import com.example.htopstore.util.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalysisActivity : AppCompatActivity() {
    private  lateinit var productAnalysisFragment: ProductAnalysisFragment
    private  lateinit var salesAnalysisFragment: SalesAnalysisFragment
    private  lateinit var accountantFrag: AccountantFragment
    private lateinit var binding: ActivityAnalysisBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAdapter()
    }
    private fun setAdapter(){
        //init
        productAnalysisFragment = ProductAnalysisFragment()
        salesAnalysisFragment = SalesAnalysisFragment()
        accountantFrag = AccountantFragment()

        val list = listOf(productAnalysisFragment,salesAnalysisFragment,accountantFrag)
        binding.viewPager.adapter = ViewPagerAdapter(this,list)
        binding.viewPager.isUserInputEnabled = false
        TabLayoutMediator(binding.tabLo, binding.viewPager){
            tab,position ->
            when(position){
                0 -> tab.text = "Product"
                1 -> tab.text = "Sales"
                2 -> tab.text = "Accountant"
        }
    }.attach()

    }


}