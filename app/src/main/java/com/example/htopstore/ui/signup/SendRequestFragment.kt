package com.example.htopstore.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentSendRequestBinding

class SendRequestFragment private constructor(): Fragment() {
    private lateinit var binding: FragmentSendRequestBinding
    private lateinit var oNextStep:(code:String)->Unit
    private val vm: SignupViewModel by activityViewModels()

    companion object{
        fun newInstance(): SendRequestFragment {
            return SendRequestFragment()
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
        binding = FragmentSendRequestBinding.inflate(inflater, container, false)
        binding.sendBtn.setOnClickListener {
            val code = binding.reqET.text.toString()
            if(vm.validCode(code)){
                oNextStep(code)
            }
        }
        return binding.root
    }
    fun setOnNext(doThis:(code:String)->Unit){
        oNextStep = doThis
    }


}