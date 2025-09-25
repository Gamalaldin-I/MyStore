package com.example.htopstore.ui.dayDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.model.SoldProduct
import com.example.htopstore.databinding.FragmentReturnsBinding
import com.example.htopstore.util.adapters.ReturnsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReturnsFragment private constructor(): Fragment() {
    private lateinit var binding: FragmentReturnsBinding
    private val vm: DayDetailsViewModel by activityViewModels()

    companion object{
        fun newInstance(day: String):ReturnsFragment {
            val args = Bundle()
            args.putString("day", day)
            val fragment = ReturnsFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentReturnsBinding.inflate(inflater, container, false)
        val day = arguments?.getString("day")
        getReturnsOfDay(day!!)
        return binding.root
    }

    private fun getReturnsOfDay(date:String){
        vm.getReturnsOfDay(date)
        vm.returns.observe(viewLifecycleOwner){
            binding.recyclerView.adapter = ReturnsAdapter(it as ArrayList<SoldProduct>){

            }
        }

    }

}