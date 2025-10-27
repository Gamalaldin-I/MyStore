package com.example.htopstore.ui.changePassword

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityCahngePasswordBinding
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Suppress("DEPRECATION")
class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCahngePasswordBinding
    private val vm: ChangePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCahngePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViews()
    }


    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.isNotEmpty()) {
                    updatePasswordStrength(password)
                    binding.llPasswordStrength.visibility = View.VISIBLE
                } else {
                    binding.llPasswordStrength.visibility = View.GONE
                }

                // Clear error when user types
                binding.tilNewPassword.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Confirm password field - validate match
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilConfirmPassword.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Current password field
        binding.etCurrentPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilCurrentPassword.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Change password button
        binding.btnChangePassword.setOnClickListener {
            attemptChangePassword()
        }

        // Forgot password link
        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password or show dialog
            DialogBuilder.showForgotPasswordDialog(
                context = this,
                onConfirm = {
                    vm.forgotPassword{
                        DialogBuilder.showSuccessDialog(
                            context = this,
                            message = "Password reset link sent to your email",
                            onConfirm = {
                                finish()
                            }
                        )
                    }
                }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePasswordStrength(password: String) {
        val strength = vm.calculatePasswordStrength(password)

        binding.pbPasswordStrength.progress = strength.score
        binding.tvPasswordStrength.text = "Password Strength: ${strength.label}"

        // Update color based on strength
        val colorRes = when (strength.score) {
            in 0..33 -> android.R.color.holo_red_dark
            in 34..66 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_green_dark
        }

        binding.pbPasswordStrength.progressTintList =
            ContextCompat.getColorStateList(this, colorRes)
    }



    private fun attemptChangePassword() {
        // Get input values
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate inputs
        if (!vm.validateInputs(currentPassword, newPassword, confirmPassword)) {
            return
        }
        // Show loading
        showLoading(true)
        vm.changePassword(currentPassword, newPassword){
            showLoading(false)
            DialogBuilder.showSuccessDialog(
                context = this,
                message = "Password changed successfully",
                onConfirm = {
                    finish()
                }
            )
        }
    }



    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnChangePassword.isEnabled = !show
        binding.btnChangePassword.text = if (show) "" else getString(R.string.change_password)

        // Disable inputs while loading
        binding.etCurrentPassword.isEnabled = !show
        binding.etNewPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
    }



    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (hasUnsavedChanges()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Discard Changes?")
                .setMessage("Are you sure you want to go back? Your changes will be lost.")
                .setPositiveButton("Discard") { dialog, _ ->
                    dialog.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        return binding.etCurrentPassword.text.toString().isNotEmpty() ||
                binding.etNewPassword.text.toString().isNotEmpty() ||
                binding.etConfirmPassword.text.toString().isNotEmpty()
    }
    private fun observeViews(){
        vm.currentError.observe(this){
            binding.tilCurrentPassword.error = it
        }
        vm.newError.observe(this){
            binding.tilNewPassword.error = it
        }
        vm.confirmError.observe(this){
            binding.tilConfirmPassword.error = it
        }
        vm.msg.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

    }

    data class PasswordStrength(val score: Int, val label: String)
}