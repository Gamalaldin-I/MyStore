package com.example.htopstore.ui.days

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
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
        private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
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
            if(it == null || it.isEmpty()) {
                onEmptyState(true)
                return@observe
            }
            onEmptyState(false)
            adapter.updateData(it as ArrayList)
            binding.selectDay.text = ".."
            binding.date.text = "All Days"
        }
        vm.specificDay.observe(this){
            if(it == null || it.isEmpty()) {
                onEmptyState(true)
                return@observe
            }
            onEmptyState(false)
            adapter.updateData(arrayListOf(it))
            binding.date.text = it
        }
    }
    private fun onEmptyState(empty: Boolean){
        if(empty){
        binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
        else{
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

}