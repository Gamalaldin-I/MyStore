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
import com.bumptech.glide.signature.ObjectKey
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentProfielBinding
import com.example.htopstore.ui.changeEmail.ChangeEmailActivity
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.updateStore.UpdateStoreActivity
import com.example.htopstore.util.DataValidator.isValidName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfielBinding? = null
    private val binding get() = _binding!!
    private val vm: MainViewModel by activityViewModels()

    private var isEditing = false
    private var hasChanges = false

    // 🔹 Image picker
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            handleSelectedImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfielBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        showData()
        setupListeners()
    }

    private fun setupUI() {
        setEditingEnabled(false)
    }

    private fun showData() {
        val user = vm.getUserData()
        val store = vm.getStoreData()

        binding.apply {
            profileNameTV.text = user.name
            profileEmailTV.text = user.email
            roleChip.text = vm.getRole()
            nameEt.setText(user.name)
            storeNameTV.text = store.name
            storeLocationTV.text = store.location
            storePhoneTV.text = store.phone

            Glide.with(profileAvatar.context)
                .load(vm.getProfileImage())
                .placeholder(R.drawable.icon_profile)
                .error(R.drawable.icon_profile)
                .into(profileAvatar)
        }

        Log.d("ProfileFragment", "Loaded data: ${store.name}, ${store.phone}")
    }

    private fun setupListeners() {
        binding.apply {
            if (vm.isLoginFromGoogle()) {
                changeEmailAction.visibility = View.GONE
                editPhotoBtn.visibility = View.GONE
                finishBtn.visibility = View.GONE
            }

            editPhotoBtn.setOnClickListener { showPhotoOptions() }
            callBtn.setOnClickListener { makePhoneCall(storePhoneTV.text.toString()) }
            changeEmailAction.setOnClickListener { startActivity(Intent(requireContext(), ChangeEmailActivity::class.java)) }
            updateStoreAction.setOnClickListener { startActivity(Intent(requireContext(), UpdateStoreActivity::class.java)) }
            logout.setOnClickListener { showLogoutConfirmation() }

            finishBtn.setOnClickListener {
                if (!isEditing) setEditingEnabled(true)
                else if (validateInputs()) updateProfile()
            }

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
                        binding.profileAvatar.setImageResource(R.drawable.icon_profile)
                        vm.removeProfilePhoto()
                    }
                    else -> dialog.dismiss()
                }
            }.show()
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            Glide.with(binding.profileAvatar.context).clear(binding.profileAvatar)
            Glide.with(binding.profileAvatar.context)
                .load(uri)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.profileAvatar)

            vm.changePhoto(uri){}

        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error handling selected image: ${e.message}", e)
            Toast.makeText(requireContext(), "Failed to update photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Phone number not available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL).apply { data = "tel:$phoneNumber".toUri() }
        startActivity(intent)
    }

    private fun validateInputs(): Boolean {
        val name = binding.nameEt.text.toString().trim()
        return when {
            name.isEmpty() -> {
                binding.nameLo.error = "Name is required"; false
            }
            !name.isValidName() -> {
                binding.nameLo.error = "Invalid name"; false
            }
            else -> {
                binding.nameLo.error = null; true
            }
        }
    }

    private fun updateProfile() {
        if (!hasChanges) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.nameEt.text.toString().trim()
        showLoading(true)

        vm.updateName(name) {
            showLoading(false)
            binding.profileNameTV.text = name
            hasChanges = false
            setEditingEnabled(false)
        }
    }

    private fun setEditingEnabled(enabled: Boolean) {
        isEditing = enabled
        binding.nameEt.isEnabled = enabled
        binding.finishBtn.text = if (enabled) "Save" else "Edit"
    }

    private fun showLoading(show: Boolean) {
        binding.finishBtn.isEnabled = !show
        binding.finishBtn.text = if (show) "Saving..." else "Save"
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss(); performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
