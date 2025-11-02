package com.example.htopstore.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.htopstore.databinding.FragmentProfielBinding
import com.example.htopstore.ui.changeEmail.ChangeEmailActivity
import com.example.htopstore.ui.changePassword.ChangePasswordActivity
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.updateStore.UpdateStoreActivity
import com.example.htopstore.util.DataValidator.isValidName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfielBinding
    private val vm: MainViewModel by activityViewModels()

    private var isEditing = false
    private var hasChanges = false

    // Image picker for profile photo
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.profileAvatar.setImageURI(it)
            // TODO: Upload image to server
            // vm.uploadProfilePhoto(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        showData()
        setupListeners()
    }

    private fun setupUI() {
        // Initially disable editing
        setEditingEnabled(false)
    }

    private fun showData() {
        val user = vm.getUserData()
        val store = vm.getStoreData()

        // Update UI with user data
        binding.apply {
            // Profile header
            profileNameTV.text = user.name
            profileEmailTV.text = user.email
            roleChip.text = vm.getRole()

            // Personal information
            nameEt.setText(user.name)

            // Store information
            storeNameTV.text = store.name
            storeLocationTV.text = store.location
            storePhoneTV.text = store.phone
        }

        Log.d("ProfileFragment", "showData - Location: ${store.location}, Phone: ${store.phone}")
    }

    private fun setupListeners() {
        binding.apply {
            if(vm.isLoginFromGoogle()){
                changeEmailAction.visibility = View.GONE
                changePasswordAction.visibility = View.GONE
                editPhotoBtn.visibility = View.GONE
                Glide.with(profileAvatar.context)
                    .load(vm.getProfileImage())
                    .error(com.example.htopstore.R.drawable.icon_profile)
                    .placeholder(com.example.htopstore.R.drawable.icon_profile)
                    .into(profileAvatar)
            }

            // Edit photo button
            editPhotoBtn.setOnClickListener {
                showPhotoOptions()
            }

            // Call store button
            callBtn.setOnClickListener {
                val phoneNumber = storePhoneTV.text.toString()
                makePhoneCall(phoneNumber)
            }

            // Change password action
            changePasswordAction.setOnClickListener {
                startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
            }

            changeEmailAction.setOnClickListener {
                startActivity(Intent(requireContext(), ChangeEmailActivity::class.java))
            }
            updateStoreAction.setOnClickListener{
                startActivity(Intent(requireContext(), UpdateStoreActivity::class.java))
            }

            // Logout button
            logout.setOnClickListener {
                showLogoutConfirmation()
            }

            // Update profile button
            finishBtn.setOnClickListener {
                if(!isEditing) { setEditingEnabled(true)}
                else{
                    if (validateInputs()) {
                        updateProfile()
                    }
                }

            }


            // Track text changes for validation
            nameEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    hasChanges = true
                    nameLo.error = null
                }
                override fun afterTextChanged(s: Editable?) {}
            })

        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Choose from Gallery", "Remove Photo", "Cancel")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> {
                        // Reset to default avatar
                        binding.profileAvatar.setImageResource(com.example.htopstore.R.drawable.icon_profile)
                        // TODO: Remove photo from server
                        // vm.removeProfilePhoto()
                    }
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$phoneNumber".toUri()
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Phone number not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setEditingEnabled(enabled: Boolean) {
        isEditing = enabled
        binding.apply {
            nameEt.isEnabled = enabled
            finishBtn.text = if (enabled) "Update now" else "Enable Editing"
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        binding.apply {
            // Validate name
            val name = nameEt.text.toString().trim()
            if(!name.isValidName()){
                nameLo.error = "Invalid name"
                isValid = false

            }
            else if (name.isEmpty()) {
                nameLo.error = "Valid name is required"
                isValid = false
            }
            else{
                nameLo.error = null
            }

        }

        return isValid
    }

    private fun updateProfile() {
        if (!hasChanges) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.nameEt.text.toString().trim()

        // Show loading state
        showLoading(true)
        vm.updateName(name){
            showLoading(false)
            handleUpdateSuccess(name)
        }
    }

    private fun handleUpdateSuccess(name: String) {
        // Update header with new data
        binding.profileNameTV.text = name
        hasChanges = false
        setEditingEnabled(false)
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            finishBtn.isEnabled = !show
            finishBtn.text = if (show) "" else "Update Name"

        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()

        vm.logout { success, msg ->
            if (success) {
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}