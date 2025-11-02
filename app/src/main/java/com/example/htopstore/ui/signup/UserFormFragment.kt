package com.example.htopstore.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentFormBinding

class UserFormFragment (): Fragment() {
    private lateinit var binding: FragmentFormBinding
    private val vm: SignupViewModel by activityViewModels()
    private lateinit var oNextStep:(name:String,email:String,password:String)->Unit
    private lateinit var onSignUpWithGoogle:()->Unit
    companion object{
        fun newInstance(): UserFormFragment {
            return UserFormFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentFormBinding.inflate(inflater, container, false)
        binding.finishBtn.setOnClickListener {
            allFieldsAreValid()
        }
        binding.googleSignInBtn.setOnClickListener{
            onSignUpWithGoogle()
        }
        return binding.root
    }
    private fun allFieldsAreValid():Boolean{
        val name = binding.nameEt.text.toString()
        val email = binding.emailEt.text.toString()
        val pass = binding.passwordEt.text.toString()
        val confirmPass = binding.confirmPasswordEt.text.toString()
        if(vm.isUserDataValid(name,email,pass,confirmPass)){
            oNextStep(name,email,pass)
        }
        return false

        }
    fun setOnNext(doThis:(name:String,email:String,password:String)->Unit){
        oNextStep = doThis
    }
    fun setONSignWithGoogle(
        doThis:()->Unit
    ){
        onSignUpWithGoogle = doThis
    }

}