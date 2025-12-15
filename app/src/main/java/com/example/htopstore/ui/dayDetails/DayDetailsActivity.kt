package com.example.htopstore.ui.dayDetails

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.useCase.localize.NAE.ae
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityDayDetailsBinding
import com.example.htopstore.util.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DayDetailsActivity : AppCompatActivity() {


    private val vm: DayDetailsViewModel by viewModels()
    private lateinit var  billFragment: BillsFragment
    private lateinit var  returnsFragment: ReturnsFragment
    private lateinit var  expensesFragment: ExpensesFragment
    private lateinit var binding: ActivityDayDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDayDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observe()
        val day = intent.getStringExtra("day")
        if (day != null) {
            getDetails(day)
            setViewPager(day)
        }
    }

    private fun setViewPager(day:String){
        billFragment = BillsFragment.newInstance(day = day )
        returnsFragment = ReturnsFragment.newInstance(day = day)
        expensesFragment = ExpensesFragment.newInstance(date = day)
        val list = listOf(billFragment,returnsFragment,expensesFragment)
        val adapter = ViewPagerAdapter(this,list)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLo, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.bills)
                1 -> tab.text = getString(R.string.returns)
                2 -> tab.text = getString(R.string.expenses)
            }
        }.attach()
    }


    private fun getDetails(day:String){
        vm.getExpensesOfDay(day)
        vm.getProfitOfDay(day)
        vm.getTotalSalesOfDay(day)
    }

    private fun observe(){
        vm.profit.observe(this){ it ->
            val profit = it ?: 0.0
            binding.dayDet.profit.text = profit.ae()
        }
        vm.totalSales.observe(this){
            val total =it ?: 0.0
            binding.dayDet.salesValue.text = total.ae()
        }
        vm.expenses.observe(this){
            val expenses = it ?: 0.0
            binding.dayDet.expenses.text = expenses.ae()
        }
    }

}