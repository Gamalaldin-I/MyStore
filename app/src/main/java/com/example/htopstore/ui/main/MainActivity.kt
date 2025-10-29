package com.example.htopstore.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.domain.util.Constants
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityMainBinding
import com.example.htopstore.ui.inbox.InboxActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showOfArchiveLen()
        val cartFragment = CartFragment()
        val stockFragment = StockFragment()
        val main = HomeFragment()
        val archive = ArchiveFragment()
        val profile = ProfileFragment()
        replaceCurrentFragment(stockFragment)
        //to listen if the employee status changed
        onEmployeeStatusChanged()
        vm.message.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }


        //set the bottom navigation view
        binding.mainNavigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.stock -> {
                    replaceCurrentFragment(stockFragment)
                    true
                }

                R.id.main -> {
                    replaceCurrentFragment(main)
                    true
                }

                R.id.cart -> {
                    replaceCurrentFragment(cartFragment)
                    true
                }

                R.id.archive -> {
                    replaceCurrentFragment(archive)
                    true
                }

                R.id.profile -> {
                    replaceCurrentFragment(profile)
                    true
                }

                else -> false
            }
        }
    }

    private fun showOfArchiveLen() {
        vm.archiveSize.observe(this) {
            if (it == 0) {
                binding.mainNavigationBar.removeBadge(R.id.archive)
                return@observe
            }
            var badge = binding.mainNavigationBar.getOrCreateBadge(R.id.archive)
            badge.isVisible = true
            badge.text = it.toString()
        }
    }

    fun replaceCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainFrameLayout, fragment)
            commit()
        }
    }

    fun onEmployeeStatusChanged() {
        vm.startListening()
        vm.employeeStatus.observe(this) {
            Log.d("MainActivity1", "onEmployeeStatusChanged: $it")
            if (it != Constants.STATUS_HIRED) {
                // go to inbox and clear the store data
                vm.clearStoreData()
                startActivity(Intent(this@MainActivity, InboxActivity::class.java))
                finish()
            }
        }
    }


}