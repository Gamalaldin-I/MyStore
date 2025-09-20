package com.example.htopstore.ui.adding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityAddPactBinding
import com.example.htopstore.util.helper.AutoCompleteHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
@AndroidEntryPoint
class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPactBinding
    private val viewModel: AddProductViewModel by viewModels()

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val tempUri = viewModel.getTempImageUri()
                binding.productImg.setImageURI(tempUri)
                Toast.makeText(this, "Captured!", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPactBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupUI()
        observeViewModel()
        setControllers()
    }

    private fun setupUI() {
        // Set the adapter for categories
        binding.autoTypeCompleteQ1.setAdapter(
            AutoCompleteHelper.getCategoriesAdapter(this)
        )
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            updateUI(state)
        }

        viewModel.validationMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is NavigationEvent.NavigateBack -> finish()
            }
        }
    }

    private fun updateUI(state: AddProductUiState) {
        // Show/hide loading indicator
        //binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Enable/disable add button during loading
        binding.add.isEnabled = !state.isLoading

        // Clear form if needed
        if (state.shouldClearForm) {
            clearForm()
        }

        // Show success message and optionally finish activity
        if (state.productSaved) {
            Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
            // Optionally finish the activity or keep it open for adding more products
            // finish()
        }
    }

    private fun clearForm() {
        binding.apply {
            productBrandET.text?.clear()
            buyingPriceET.text?.clear()
            sellingPriceET.text?.clear()
            countET.text?.clear()
            autoTypeCompleteQ1.text?.clear()
            productImg.setImageResource(R.drawable.ic_camera)
        }
    }

    private fun setControllers() {
        binding.productImg.setOnClickListener {
            askCameraPermission()
        }

        binding.add.setOnClickListener {
            viewModel.validateAndSaveProduct(
                context = this,
                category = binding.autoTypeCompleteQ1.text.toString(),
                brand = binding.productBrandET.text.toString(),
                buyingPrice = binding.buyingPriceET.text.toString(),
                sellingPrice = binding.sellingPriceET.text.toString(),
                count = binding.countET.text.toString()
            )
        }

        binding.backArrow.setOnClickListener {
            viewModel.onBackPressed()
        }
    }

    private fun askCameraPermission() {
        when {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile: File = viewModel.createTempImageFile(this)
        val photoUri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        // Store temp file data in ViewModel
        viewModel.setTempImageData(photoFile, photoUri)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }
}