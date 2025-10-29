package com.example.htopstore.ui.changeEmail

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityChangeEmailBinding
import com.example.htopstore.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeEmailBinding
    private val viewModel : ChangeEmailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Display current email
        binding.tvCurrentEmail.text = viewModel.getOldEmail()

        // Setup text watchers for real-time validation
        binding.etNewEmail.doAfterTextChanged {
            binding.tilNewEmail.error = null
        }

        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Change email button
        binding.btnChangeEmail.setOnClickListener {
            if (validateInputs()) {
                changeEmail()
            }
        }
        viewModel.msg.observe(this){
            showToast(it)
        }
    }

    private fun validateInputs(): Boolean {
        val newEmail = binding.etNewEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val currentEmail = viewModel.getOldEmail()

        var isValid = true

        // Validate new email
        if (newEmail.isEmpty()) {
            binding.tilNewEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            binding.tilNewEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else if (newEmail == currentEmail) {
            binding.tilNewEmail.error = getString(R.string.error_same_email)
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        }

        return isValid
    }

    private fun changeEmail() {
        val newEmail = binding.etNewEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        // Show loading
        showLoading(true)
            viewModel.changeEmail(newEmail, password, onFail = {
                showLoading(false)
            }){
                showLoading(false)
                showSuccessDialog()
            }

    }


    private fun showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.success)
            .setMessage(R.string.email_change_success_message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.logout()
                Toast.makeText(this, "Login with new email", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnChangeEmail.isEnabled = !isLoading
        binding.etNewEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.btnBack.isEnabled = !isLoading

        if (isLoading) {
            binding.btnChangeEmail.text = ""
        } else {
            binding.btnChangeEmail.text = getString(R.string.change_email_button)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}