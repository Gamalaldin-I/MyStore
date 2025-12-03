package com.example.htopstore.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityProfileBinding
import com.example.htopstore.ui.changeEmail.ChangeEmailActivity
import com.example.htopstore.ui.createStore.CreateStoreActivity
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.util.BaseActivity
import com.example.htopstore.util.DataValidator.isValidName
import com.example.htopstore.util.helper.PermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity:BaseActivity(){

    private lateinit var binding: ActivityProfileBinding
    private val vm: ProfileViewModel by viewModels()

    private var isEditing = false
    private var hasChanges = false

    // Pick Image Launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleSelectedImage(it) }
        }

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        showData()
    }

    // ---------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------

    private fun setupUI() {
        setEditingEnabled(false)
       if(!PermissionHelper.isAdmin(vm.role)){
            binding.updateStoreAction.visibility = View.GONE
        }
    }

    private fun setupListeners() = binding.apply {

        // Google Login Restrictions
        if (vm.isLoginFromGoogle()) {
            changeEmailAction.visibility = View.GONE
            editPhotoBtn.visibility = View.GONE
            finishBtn.visibility = View.GONE
        }


        editPhotoBtn.setOnClickListener { showPhotoOptions() }
        callBtn.setOnClickListener { makePhoneCall(storePhoneTV.text.toString()) }
        changeEmailAction.setOnClickListener { openActivity(ChangeEmailActivity::class.java) }

        updateStoreAction.setOnClickListener {
            val intent = Intent(this@ProfileActivity, CreateStoreActivity::class.java)
            intent.putExtra("fromUpdate", true)
            startActivity(intent)
        }

        logout.setOnClickListener { showLogoutConfirmation() }

        finishBtn.setOnClickListener {
            if (!isEditing) setEditingEnabled(true)
            else if (validateInputs()) updateProfile()
        }

        nameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasChanges = true
                nameLo.error = null
            }
        })
    }

    // ---------------------------------------------------------------------------
    // UI Data
    // ---------------------------------------------------------------------------

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

        Log.d("ProfileActivity", "Loaded store: ${store.name}, ${store.phone}")
    }

    // ---------------------------------------------------------------------------
    // Image Handling
    // ---------------------------------------------------------------------------

    private fun showPhotoOptions() {
        val options = arrayOf(
            getString(R.string.choose_from_gallery),
            getString(R.string.remove_photo),
            getString(R.string.cancel)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.change_profile_photo))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> {
                        binding.profileAvatar.setImageResource(R.drawable.icon_profile)
                        vm.removeProfilePhoto()
                    }
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val avatar = binding.profileAvatar

            Glide.with(avatar.context).clear(avatar)
            Glide.with(avatar.context)
                .load(uri)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(avatar)

            vm.changePhoto(uri)

        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error image: ${e.message}", e)
            Toast.makeText(this, R.string.failed_to_update_photo, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------------------------
    // Input Validation + Saving
    // ---------------------------------------------------------------------------

    private fun validateInputs(): Boolean {
        val name = binding.nameEt.text.toString().trim()

        return when {
            name.isEmpty() -> {
                binding.nameLo.error = getString(R.string.name_required)
                false
            }
            !name.isValidName() -> {
                binding.nameLo.error = getString(R.string.invalid_name)
                false
            }
            else -> true
        }
    }

    private fun updateProfile() {
        if (!hasChanges) {
            Toast.makeText(this, R.string.no_changes, Toast.LENGTH_SHORT).show()
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

    // ---------------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------------

    private fun setEditingEnabled(enabled: Boolean) {
        isEditing = enabled
        binding.nameEt.isEnabled = enabled
        binding.finishBtn.text = if (enabled) getString(R.string.save) else getString(R.string.edit)
    }

    private fun showLoading(show: Boolean) {
        binding.finishBtn.isEnabled = !show
        binding.finishBtn.text =
            if (show) getString(R.string.saving) else getString(R.string.save)
    }

    private fun makePhoneCall(phone: String) {
        if (phone.isEmpty()) {
            Toast.makeText(this, R.string.phone_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phone".toUri()
        })
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout) { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun performLogout() {
        Toast.makeText(this, R.string.logging_out, Toast.LENGTH_SHORT).show()

        vm.logout { success, _ ->
            if (success) {
                startActivity(
                    Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                finish()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun <T> openActivity(cls: Class<T>) {
        startActivity(Intent(this, cls))
    }
}
