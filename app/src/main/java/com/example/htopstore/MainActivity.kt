package com.example.htopstore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.databinding.ActivityMainBinding
import com.example.htopstore.ui.archive.ArchiveFragment
import com.example.htopstore.ui.cart.CartFragment
import com.example.htopstore.ui.main.HomeFragment
import com.example.htopstore.ui.stock.StockFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var productRepo: ProductRepoImp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val cartFragment = CartFragment()
        val stockFragment = StockFragment()
        val main = HomeFragment()
        val archive = ArchiveFragment()
        productRepo = ProductRepoImp(this)
        replaceCurrentFragment(stockFragment)


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

                else -> false
            }
            }
    }
    private fun showOfArchiveLen(){
        lifecycleScope.launch(Dispatchers.IO) {
            val len = productRepo.getArchiveLength()
            runOnUiThread {
                if (len>0){
                var badge = binding.mainNavigationBar.getOrCreateBadge(R.id.archive)
                badge.isVisible = true
                badge.text = len.toString()}
        }
        }
    }

    fun replaceCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainFrameLayout, fragment)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        showOfArchiveLen()
    }
}