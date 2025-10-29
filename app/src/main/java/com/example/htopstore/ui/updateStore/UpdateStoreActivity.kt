package com.example.htopstore.ui.updateStore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.databinding.ActivityUpdateStoreBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateStoreBinding
    private val viewModel: UpdateStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
        setupObservers()
        viewModel.loadStoreData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViews() {
        // Text change listeners
        binding.nameEditText.addTextChangedListener {
            binding.nameInputLayout.error = null
        }

        binding.phoneEditText.addTextChangedListener {
            binding.phoneInputLayout.error = null
        }

        binding.locationEditText.addTextChangedListener {
            binding.locationInputLayout.error = null
        }

        // Buttons
        binding.updateButton.setOnClickListener {
            if (validateInputs()) {
                val name = binding.nameEditText.text.toString()
                val phone = binding.phoneEditText.text.toString()
                val location = binding.locationEditText.text.toString()
                showConfirmationDialog(name=name, phone=phone, location =location)
            }
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // Loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                setInputsEnabled(!isLoading)
            }
        }

        // toast
            viewModel.msg.observe(this){ result ->
                Toast.makeText(this@UpdateStoreActivity, result, Toast.LENGTH_SHORT).show() }

        viewModel.loadStoreData().let{
            binding.nameEditText.setText(it.name)
            val phoneNumber = if(it.phone.startsWith("+2")){
                it.phone
            }else{
                "+2${it.phone}"
            }
            binding.phoneEditText.setText(phoneNumber)
            binding.locationEditText.setText(it.location)
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.updateButton.isEnabled = enabled
        binding.cancelButton.isEnabled = enabled
        binding.nameEditText.isEnabled = enabled
        binding.phoneEditText.isEnabled = enabled
        binding.locationEditText.isEnabled = enabled
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = binding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Store name is required"
            isValid = false
        } else if (name.length < 3) {
            binding.nameInputLayout.error = "Store name must be at least 3 characters"
            isValid = false
        }

        val phone = binding.phoneEditText.text.toString().trim()
        if (phone.isEmpty()) {
            binding.phoneInputLayout.error = "Phone number is required"
            isValid = false
        } else if (!isValidPhoneNumber(phone)) {
            binding.phoneInputLayout.error = "Invalid phone number format"
            isValid = false
        }

        val location = binding.locationEditText.text.toString().trim()
        if (location.isEmpty()) {
            binding.locationInputLayout.error = "Location is required"
            isValid = false
        } else if (location.length < 5) {
            binding.locationInputLayout.error = "Please provide a complete address"
            isValid = false
        }

        return isValid
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^[+]?[0-9]{10,15}$".toRegex()
        return phone.replace("\\s".toRegex(), "").matches(phoneRegex)
    }

    private fun showConfirmationDialog(name: String, phone: String, location: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to update the store information?")
            .setPositiveButton("Update") { _, _ ->
                viewModel.updateStore(
                    name = name,
                    phone = phone,
                    location = location,
                    actionOnSuccess = {})
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}