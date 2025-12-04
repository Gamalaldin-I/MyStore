package com.example.htopstore.ui.emlpoyee

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.domain.util.Constants
import com.example.htopstore.databinding.ActivityEmployeeBinding
import com.example.htopstore.util.BaseActivity
import com.example.htopstore.util.helper.AutoCompleteHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmployeeActivity : BaseActivity() {
    private lateinit var binding: ActivityEmployeeBinding
    private val viewModel: EmployeeViewModel by viewModels()

    private var employeeId: String? = null
    private var currentRole: Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRoleDropdown()
        setupClickListeners()
        observeViewModel()
        viewEmpDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRoleDropdown() {
        binding.roleOptions.setAdapter(AutoCompleteHelper.getRolesAdapter(this))
    }

    private fun setupClickListeners() {
        binding.saveChanges.setOnClickListener {
            handleSaveChanges()
        }

        binding.fire.setOnClickListener {
            showTerminationDialog()
        }
    }

    private fun viewEmpDetails() {
        employeeId = intent.getStringExtra("employee_id")
        val name = intent.getStringExtra("employee_name")
        val email = intent.getStringExtra("employee_email")
        val role = intent.getIntExtra("employee_role", 4)
        val status = intent.getStringExtra("employee_status")
        val photoUrl = intent.getStringExtra("employee_photo_url")

        currentRole = role

        // Set data
        binding.name.text = name
        binding.email.text = email
        binding.role.text = Constants.getRoleName(role)

        // Update status chip
        val isActive = status == Constants.STATUS_HIRED
        binding.status.text = if (isActive) "Active" else "Inactive"
        binding.status.setChipBackgroundColorResource(
            if (isActive) com.example.htopstore.R.color.action_success
            else com.example.htopstore.R.color.neutral_500
        )

        // Update fire button text based on status
        binding.fire.text = if (isActive) "Terminate Employee" else "Rehire Employee"

        // Load photo with Glide
        Glide.with(this@EmployeeActivity)
            .load(photoUrl)
            .circleCrop()
            .placeholder(com.example.htopstore.R.drawable.icon_profile)
            .error(com.example.htopstore.R.drawable.icon_profile)
            .into(binding.photo)
    }

    private fun handleSaveChanges() {
        val selectedRoleText = binding.roleOptions.text.toString()

        if (selectedRoleText.isEmpty()) {
            Snackbar.make(binding.root, "Please select a role", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Convert role name to role ID
        val newRole = Constants.getRoleId(selectedRoleText)

        if (newRole == currentRole) {
            Snackbar.make(binding.root, "No changes to save", Snackbar.LENGTH_SHORT).show()
            return
        }

        employeeId?.let { empId ->
            viewModel.changeEmployeeRole(empId, newRole) { successMessage ->
                // Update current role and UI on success
                currentRole = newRole
                binding.role.text = Constants.getRoleName(newRole)
                binding.roleOptions.setText("", false)
            }
        } ?: run {
            Snackbar.make(binding.root, "Employee ID not found", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showTerminationDialog() {
        val status = intent.getStringExtra("employee_status")
        val isActive = status == Constants.STATUS_HIRED

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isActive) "Terminate Employee" else "Rehire Employee")
            .setMessage(
                if (isActive)
                    "Are you sure you want to terminate this employee? This action can be reversed later."
                else
                    "Are you sure you want to rehire this employee?"
            )
            .setPositiveButton(if (isActive) "Terminate" else "Rehire") { _, _ ->
                employeeId?.let { empId ->
                    viewModel.hireOrFire(empId, shouldFire = isActive)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.loading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        // Observe messages
        viewModel.msg.observe(this) { message ->
            if (message.isNotEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

                // Check if it's a termination/rehire success message
                if (message.contains("fired", ignoreCase = true) ||
                    message.contains("terminated", ignoreCase = true) ||
                    message.contains("hired", ignoreCase = true) ||
                    message.contains("rehired", ignoreCase = true)) {
                    // Finish activity after showing message
                    binding.root.postDelayed({
                        setResult(RESULT_OK)
                        finish()
                    }, 1500)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        // Show/hide loading overlay
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE

        // Disable/enable interactions
        binding.saveChanges.isEnabled = !show
        binding.fire.isEnabled = !show
        binding.roleOptions.isEnabled = !show
        binding.toolbar.navigationIcon?.alpha = if (show) 128 else 255
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear observers to prevent memory leaks
        viewModel.msg.removeObservers(this)
        viewModel.loading.removeObservers(this)
    }
}