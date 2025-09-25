package com.example.htopstore.ui.days

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityDaysBinding
import com.example.htopstore.ui.dayDetails.DayDetailsActivity
import com.example.htopstore.ui.widgets.DatePickerFragment
import com.example.htopstore.util.adapters.DaysAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DaysActivity : AppCompatActivity() {
    private val vm: DaysViewModel by viewModels()
    private lateinit var binding: ActivityDaysBinding
    private lateinit var adapter: DaysAdapter
    companion object {
        private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDaysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm.getDays()
        observe()
        setControllers()
        adapter = DaysAdapter(arrayListOf()){
            val intent = Intent(this,DayDetailsActivity::class.java)
            intent.putExtra("day",it)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter

    }
    private fun setControllers(){
        binding.selectDay.setOnClickListener {
                val datePicker = DatePickerFragment { day, month, year ->
                    val pickedDate = LocalDate.of(year, month, day).format(displayFormatter)
                    vm.getSpecificDay(pickedDate!!)
                }
            datePicker.show(supportFragmentManager, "datePicker")
        }
        binding.reset.setOnClickListener {
            vm.getDays()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun observe(){
        vm.days.observe(this){
            //setupAdapter
            adapter.updateData(it as ArrayList)
            binding.selectDay.text = "Select Day"
        }
        vm.specificDay.observe(this){
            if(it == null || it.isEmpty()) {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show()
                return@observe
            }
            adapter.updateData(arrayListOf(it))
            binding.selectDay.text = it
        }
    }

}