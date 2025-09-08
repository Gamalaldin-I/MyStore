package com.example.htopstore.ui.Archive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.databinding.FragmentArchiveBinding
import com.example.htopstore.util.adapters.ArchiveRecycler
import com.example.htopstore.util.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchiveFragment : Fragment() {
    private lateinit var binding : FragmentArchiveBinding
    private  var products = ArrayList<Product>()
    private lateinit var adapter: ArchiveRecycler
    private lateinit var productRepoImp: ProductRepoImp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentArchiveBinding.inflate(layoutInflater)
        val view = binding.root
        productRepoImp = ProductRepoImp(requireContext())
        adapter = ArchiveRecycler(products){ it, pos ->
            showAlertDialog(it,pos)
        }
        binding.archiveRecycler.adapter = adapter
        getUnAvailableProducts()
        return view
    }

    private fun getUnAvailableProducts(){
        lifecycleScope.launch(Dispatchers.IO) {
            products = productRepoImp.getProductsNotAvailable() as ArrayList
            if(products.isEmpty()){
                binding.emptyHint.visibility = View.VISIBLE
            }
            else{
                binding.emptyHint.visibility = View.GONE
                withContext(Dispatchers.Main){
                // Create the adapter
                adapter.updateTheList(products)
            }
        }
        }
    }


    private fun showAlertDialog(p:Product,pos:Int){
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


    private fun onDelete(p: Product,pos: Int){
        lifecycleScope.launch(Dispatchers.IO) {
            productRepoImp.deleteProductById(p.id,p.productImage)
            //delete the pic file
            withContext(Dispatchers.Main){
                products.remove(p)
                adapter.updateAfterDeletion(pos)
                if(products.isEmpty()){
                    binding.emptyHint.visibility = View.VISIBLE
                }
            }

        }
    }
}