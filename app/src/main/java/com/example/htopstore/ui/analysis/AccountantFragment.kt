package com.example.htopstore.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.htopstore.databinding.FragmentAccountantBinding

class AccountantFragment : Fragment() {
    private lateinit var binding: FragmentAccountantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountantBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

}
