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
import com.example.htopstore.util.helper.DialogBuilder
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
            data = products,
            onDelete = { it, pos -> showAlertDialog(it, pos) })
        { it ->
            goToProductActivity(it.id)
        }
        binding.archiveRecycler.adapter = adapter
        getUnAvailableProducts()
        return view
    }

    private fun getUnAvailableProducts(){
        vm.archive.observe(viewLifecycleOwner){
            if(it.isEmpty()){
                binding.emptyHint.visibility = View.VISIBLE
            }
            else{
                binding.emptyHint.visibility = View.GONE

                adapter.updateTheList(it as ArrayList<Product>)
            }
        }
    }

    private fun showAlertDialog(p: Product, pos:Int){
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            message = "Are you sure you want to delete this product?",
            title = "Delete Product",
            positiveButton = "Yes",
            negativeButton = "No",
            onConfirm = { onDelete(p,pos)  },
            onCancel = {  }
        )
    }

    private fun onDelete(p: Product, pos: Int){
        vm.deleteProduct(p.id,p.productImage){
            adapter.updateAfterDeletion(pos)
            products.remove(p)
            if(products.isEmpty()){
                binding.emptyHint.visibility = View.VISIBLE
            }
        }
    }

    private fun goToProductActivity(id: String){
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId",id)
        startActivity(intent)

    }


}