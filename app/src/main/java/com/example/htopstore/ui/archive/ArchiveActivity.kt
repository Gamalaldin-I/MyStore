package com.example.htopstore.ui.archive

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.model.Product
import com.example.htopstore.databinding.ActivityArchiveBinding
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.ArchiveRecycler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveActivity : AppCompatActivity() {
    private lateinit var binding : ActivityArchiveBinding
    private val vm: ArchiveViewModel by viewModels()
    private  var products = ArrayList<Product>()
    private lateinit var adapter: ArchiveRecycler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ArchiveRecycler(
            data = products){ it ->
            goToProductActivity(it.id)
        }
        binding.archiveRecycler.adapter = adapter


    }
    override fun onResume() {
        super.onResume()
        getUnAvailableProducts()
    }

    private fun getUnAvailableProducts(){
        vm.archive.observe(this){
            if(it.isEmpty()){
                binding.emptyLayout.visibility = View.VISIBLE
            }
            else{
                binding.emptyLayout.visibility = View.GONE

                adapter.updateTheList(it as ArrayList<Product>)
            }
        }
    }

    private fun goToProductActivity(id: String){
        val intent = Intent(this, ProductActivity::class.java)
        intent.putExtra("productId",id)
        startActivity(intent)

    }
}