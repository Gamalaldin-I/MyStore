package com.example.htopstore.ui.days

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
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
        private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDaysBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapter()
        setControllers()
        observe()

        // Fetch data on start
        vm.fetchBillsAndSalesFromRemote()
    }

    private fun setupAdapter() {
        adapter = DaysAdapter(arrayListOf()) {
            val intent = Intent(this, DayDetailsActivity::class.java)
            intent.putExtra("day", it)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setControllers() {
        binding.selectDay.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                val pickedDate = LocalDate.of(year, month, day).format(displayFormatter)
                vm.getSpecificDay(pickedDate)
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.reset.setOnClickListener {
            vm.getDays()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        vm.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoadingState()
            } else {
                hideLoadingState()
            }
        }

        vm.days.observe(this) { daysList ->
            if (daysList == null || daysList.isEmpty()) {
                onEmptyState(true)
                return@observe
            }
            onEmptyState(false)
            adapter.updateData(daysList as ArrayList)
            binding.selectDay.text = ".."
            binding.date.text = "All Days"
        }

        vm.specificDay.observe(this) { day ->
            if (day == null || day.isEmpty()) {
                onEmptyState(true)
                return@observe
            }
            onEmptyState(false)
            adapter.updateData(arrayListOf(day))
            binding.date.text = day
        }

        vm.message.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.filterCard.alpha = 0.5f
        binding.filterCard.isEnabled = false
    }

    private fun hideLoadingState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.filterCard.alpha = 1f
        binding.filterCard.isEnabled = true
    }

    private fun onEmptyState(empty: Boolean) {
        if (empty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}