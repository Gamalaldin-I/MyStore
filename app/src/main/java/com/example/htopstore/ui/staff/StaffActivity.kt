package com.example.htopstore.ui.staff

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityStaffBinding
import com.example.htopstore.util.helper.AutoCompleteHelper

class StaffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setControllers()
        setAdapterOfRoles()
    }

    private fun setControllers(){
        binding.backArrow.setOnClickListener {
            finish()
        }
    }
    private fun setAdapterOfRoles(){
        val adapter = AutoCompleteHelper.getRolesAdapter(this)
        binding.roleEt.setAdapter(adapter)
    }
}