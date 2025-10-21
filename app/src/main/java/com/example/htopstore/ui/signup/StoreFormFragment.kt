package com.example.htopstore.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentStoreFormBinding

class StoreFormFragment private constructor(): Fragment() {
    private lateinit var binding: FragmentStoreFormBinding
    private lateinit var oNextStep:(name:String,location:String,phone:String)->Unit
    private val vm: SignupViewModel by activityViewModels()

    companion object{
        fun newInstance(): StoreFormFragment {
            return StoreFormFragment()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoreFormBinding.inflate(inflater, container, false)
        binding.finishBtn.setOnClickListener {
            allFieldsAreValid()
        }
        return binding.root

    }
    private fun allFieldsAreValid(){
        val name = binding.nameEt.text.toString()
        val location = binding.LocationEt.text.toString()
        val phone = binding.PhoneEt.text.toString()
        if(vm.validStoreData(name,location,phone)){
            oNextStep(name,location,phone)
        }
    }

    fun setOnNext(doThis:(name:String,location:String,phone:String)->Unit){
        oNextStep = doThis
    }

}

