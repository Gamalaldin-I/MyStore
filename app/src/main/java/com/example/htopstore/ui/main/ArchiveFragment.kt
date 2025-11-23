package com.example.htopstore.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.model.Product
import com.example.htopstore.databinding.FragmentArchiveBinding
import com.example.htopstore.ui.product.ProductActivity
import com.example.htopstore.util.adapters.ArchiveRecycler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveFragment : Fragment() {

    private lateinit var binding : FragmentArchiveBinding
    private val vm: MainViewModel by activityViewModels()
    private  var products = ArrayList<Product>()
    private lateinit var adapter: ArchiveRecycler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentArchiveBinding.inflate(layoutInflater)
        val view = binding.root
        adapter = ArchiveRecycler(
            data = products){ it ->
            goToProductActivity(it.id)
        }
        binding.archiveRecycler.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        getUnAvailableProducts()
    }

    private fun getUnAvailableProducts(){
        vm.archive.observe(viewLifecycleOwner){
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
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId",id)
        startActivity(intent)

    }
}