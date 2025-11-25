package com.example.htopstore.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    private val cartFragment = CartFragment()
    private val stockFragment = StockFragment()
    private val mainFragment = HomeFragment()
    private var activeFragment: Fragment = mainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.mainFrameLayout, mainFragment)
            add(R.id.mainFrameLayout, stockFragment).hide(stockFragment)
            add(R.id.mainFrameLayout, cartFragment).hide(cartFragment)
        }.commit()

        // Observe messages
        vm.message.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }

        binding.mainNavigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.stock -> switchFragment(stockFragment)
                R.id.main -> switchFragment(mainFragment)
                R.id.cart -> switchFragment(cartFragment)
                else -> false
            }
        }
    }

    private fun switchFragment(target: Fragment): Boolean {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction().hide(activeFragment).show(target).commit()
            activeFragment = target
        }
        return true
    }
}
