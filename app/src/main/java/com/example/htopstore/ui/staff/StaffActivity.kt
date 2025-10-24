package com.example.htopstore.ui.staff

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityStaffBinding
import com.example.htopstore.util.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StaffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffBinding
    private lateinit var employeesFragment: EmployeesFragment
    private lateinit var invitesFragment: InvitesFragment
    private val vm: StaffViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFragmentsAdapter()
        vm.msg.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFragmentsAdapter(){
        employeesFragment = EmployeesFragment()
        invitesFragment = InvitesFragment()
        val adapter = ViewPagerAdapter(this,listOf(
            employeesFragment,
            invitesFragment
        ))
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Employees"
                1 -> tab.text = "Invites"
            }
        }.attach()
    }
}