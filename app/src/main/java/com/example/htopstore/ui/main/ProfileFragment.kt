package com.example.htopstore.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentProfielBinding
import com.example.htopstore.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfielBinding
    private val vm: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfielBinding.inflate(inflater, container, false)
        showData()
        return binding.root
    }
    private fun showData(){
        val user = vm.getUserData()
        val store = vm.getStoreData()
        binding.nameEt.setText(user.name)
        binding.passwordEt.setText(user.password)
        binding.emailEt.setText(user.email)
        binding.storeIdTV.text =store.id
        binding.storeNameTV.text =store.name
        binding.storeLocationTV.text =store.location
        binding.storePhoneTV.text =store.phone
        binding.role.text = vm.getRole()
        Log.d("TAGProfileFragment", "showData (Location): ${store.location}\n" +
                "(phone): ${store.phone}")
        binding.logout.setOnClickListener {
            vm.logout { success , msg ->
                if(success){
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                else{
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}