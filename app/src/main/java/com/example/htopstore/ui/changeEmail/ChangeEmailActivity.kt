package com.example.htopstore.ui.changeEmail

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityChangeEmailBinding
import com.example.htopstore.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeEmailBinding
    private val viewModel: ChangeEmailViewModel by viewModels()
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Show current email
        binding.tvCurrentEmail.text = viewModel.getOldEmail()

        // Clear errors on input change
        binding.etNewEmail.doAfterTextChanged { binding.tilNewEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
    }

    private fun setupListeners() = with(binding) {
        btnBack.setOnClickListener { finish() }

        btnChangeEmail.setOnClickListener {
            if (validateInputs()) changeEmail()
        }

        forgotPassword.setOnClickListener {
            viewModel.resetPassword()
        }

        viewModel.msg.observe(this@ChangeEmailActivity) {
            showSnack(it)
        }
    }

    private fun validateInputs(): Boolean {
        val newEmail = binding.etNewEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val currentEmail = viewModel.getOldEmail()

        var isValid = true

        // Validate new email
        when {
            newEmail.isEmpty() -> {
                binding.tilNewEmail.error = getString(R.string.error_email_required)
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                binding.tilNewEmail.error = getString(R.string.error_invalid_email)
                isValid = false
            }
            newEmail == currentEmail -> {
                binding.tilNewEmail.error = getString(R.string.error_same_email)
                isValid = false
            }
        }

        // Validate password
        when {
            password.isEmpty() -> {
                binding.tilPassword.error = getString(R.string.error_password_required)
                isValid = false
            }
            password.length < 6 -> {
                binding.tilPassword.error = getString(R.string.error_password_too_short)
                isValid = false
            }
        }

        return isValid
    }

    private fun changeEmail() {
        val newEmail = binding.etNewEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        showLoading(true)
        viewModel.changeEmail(
            newEmail = newEmail,
            password = password,
            onFail = { showLoading(false) }
        ) {
            showLoading(false)
            showSuccessDialog()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
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

    private fun showLoading(isLoading: Boolean) = with(binding) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnChangeEmail.isEnabled = !isLoading
        etNewEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        btnBack.isEnabled = !isLoading

        btnChangeEmail.text = if (isLoading) "" else getString(R.string.change_email_button)
    }

    private fun showSnack(message: String) {
        snackBar?.dismiss()
        snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar?.show()
    }
}
