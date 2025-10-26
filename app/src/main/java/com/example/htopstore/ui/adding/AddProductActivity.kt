package com.example.htopstore.ui.adding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityAddPactBinding
import com.example.htopstore.ui.scan.ScanActivity
import com.example.htopstore.util.BarcodeGenerator
import com.example.htopstore.util.helper.AutoCompleteHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPactBinding
    private val viewModel: AddProductViewModel by viewModels()

    // Camera launcher for taking photos
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = viewModel.getImageUri()
                if (imageUri != null) {
                    binding.productImg.setImageURI(imageUri)
                    binding.productImg.setPadding(0, 0, 0, 0)
                    binding.productImg.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Gallery launcher for selecting images
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    viewModel.setImageUri(selectedImageUri)
                    binding.productImg.setImageURI(selectedImageUri)
                    binding.productImg.setPadding(0, 0, 0, 0)
                    binding.productImg.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Camera permission launcher
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }

    // Storage permission launcher (for Android 12 and below)
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Storage permission is required!", Toast.LENGTH_SHORT).show()
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
        // Enable/disable add button during loading
        binding.add.isEnabled = !state.isLoading

        // Show loading state on button
        if (state.isLoading) {
            binding.add.text = "Saving..."
        } else {
            binding.add.text = "Add Product"
        }

        // Clear form if needed
        if (state.shouldClearForm) {
            clearForm()
        }

        // Show success message
        if (state.productSaved) {
            Toast.makeText(this, "Product added successfully!", Toast.LENGTH_LONG).show()
            // Reset the saved state to allow adding more products
            viewModel.resetFormState()
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
            productImg.setPadding(48, 48, 48, 48)
            productImg.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            id.text = "Not scanned"
            clearId.visibility = View.GONE
        }
    }

    private fun setControllers() {
        // Camera button - opens camera
        binding.takePhoto.setOnClickListener {
            askCameraPermission()
        }

        // Select Image button - opens gallery
        binding.selectImage.setOnClickListener {
            askStoragePermission()
        }

        // Product image click - show options
        binding.productImg.setOnClickListener {
            // Optionally show a dialog to choose between camera and gallery
            askCameraPermission()
        }

        // Add product button
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

        // Back button
        binding.backArrow.setOnClickListener {
            viewModel.onBackPressed()
        }

        // Scan barcode button
        binding.scanId.setOnClickListener {
            startScan()
        }

        // Clear scanned ID button
        binding.clearId.setOnClickListener {
            binding.id.text = "Not scanned"
            binding.clearId.visibility = View.GONE
            BarcodeGenerator.scannedCode = null
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

    private fun askStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ doesn't need READ_EXTERNAL_STORAGE for gallery picker
                openGallery()
            }
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile: File = viewModel.createTempCameraFile(this)
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )

            viewModel.setImageUri(photoUri)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra("fromAdding", true)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Update UI with scanned barcode if available
        if (BarcodeGenerator.scannedCode != null) {
            binding.id.text = BarcodeGenerator.scannedCode
            binding.clearId.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only clear the scanned code if we're finishing the activity permanently
        if (isFinishing) {
            BarcodeGenerator.scannedCode = null
        }
    }
}