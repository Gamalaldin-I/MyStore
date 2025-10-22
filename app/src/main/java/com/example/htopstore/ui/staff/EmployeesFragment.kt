package com.example.htopstore.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.htopstore.databinding.FragmentEmployeesBinding
import com.example.htopstore.util.adapters.EmployeeAdapter

class EmployeesFragment : Fragment() {

    private lateinit var binding: FragmentEmployeesBinding
    private lateinit var adapter: EmployeeAdapter
    private val vm: StaffViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEmployeesBinding.inflate(inflater, container, false)

        setupEmployeesRecyclerView()
        observeChanges()
        return binding.root
    }
    fun observeChanges(){
        vm.getEmployees()
        lifecycleScope.launchWhenCreated {
            vm.employees.collect { list ->
                if(list.isNotEmpty()){
                adapter.updateData(list as MutableList<StoreEmployee>)
                }
            }
        }

    }
    fun setupEmployeesRecyclerView() {
        adapter = EmployeeAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter
    }


}