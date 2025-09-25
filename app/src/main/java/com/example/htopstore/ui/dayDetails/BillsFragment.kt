package com.example.htopstore.ui.dayDetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentReturnsBinding
import com.example.htopstore.ui.billDetails.BillDetailsActivity
import com.example.htopstore.util.adapters.BillsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BillsFragment private constructor(): Fragment() {
    private val vm: DayDetailsViewModel by activityViewModels()
    companion object{
        fun newInstance(day: String):BillsFragment{
            val args = Bundle()
            args.putString("day",day)
            val fragment = BillsFragment()
            fragment.arguments = args
            return fragment
        }
    }



    private lateinit var binding: FragmentReturnsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentReturnsBinding.inflate(inflater, container,false)
        val day = arguments?.getString("day")
        getBillsOfDay(day!!)
        return binding.root
    }

    fun getBillsOfDay(date:String){
        vm.getBillsOfDay(date)
        vm.bills.observe(viewLifecycleOwner){
            binding.recyclerView.adapter = BillsAdapter(it){ saleId ->
                navigateToBillDetails(saleId)
            }
        }

    }
    private fun navigateToBillDetails(saleId: String) {
        val intent = Intent(requireContext(), BillDetailsActivity::class.java).apply {
            putExtra("saleId", saleId)
        }
        startActivity(intent)
    }



}