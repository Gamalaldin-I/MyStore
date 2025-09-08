package com.example.htopstore.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.R
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.databinding.ActivityAddPactBinding
import com.example.htopstore.domain.useCase.GetAdapterOfOptionsUseCase
import com.example.htopstore.util.DateHelper
import com.example.htopstore.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var pRepo: ProductRepoImp
    private lateinit var binding: ActivityAddPactBinding
    private var product: Product? = null
    private var productImageTemp: Uri? = null
    private var tempImageFile: File? = null
    private var finalImageFile: File? = null
    private lateinit var productImageView: ImageView
    private var id: String = ""

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                productImageView.setImageURI(productImageTemp)
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
        pRepo = ProductRepoImp(this)
        productImageView = binding.productImg
        id = IdGenerator.generateProductId()

        setTheAdapter()
        setControllers()
    }

    private fun setControllers() {
        productImageView.setOnClickListener {
            askCameraPermission()
        }

        binding.add.setOnClickListener {
            if (allFieldsDone()) {
                val savedFile = saveFinalImage()
                if (savedFile != null) {
                    product = Product(
                        id = id,
                        addingDate = DateHelper.getCurrentDate(),
                        productImage = savedFile.absolutePath,
                        category = binding.autoTypeCompleteQ1.text.toString(),
                        name = binding.productBrandET.text.toString(),
                        buyingPrice = binding.buyingPriceET.text.toString().toDouble(),
                        sellingPrice = binding.sellingPriceET.text.toString().toDouble(),
                        count = binding.countET.text.toString().toInt(),
                        soldCount = 0
                    )
                    saveProduct(product!!)
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.backArrow.setOnClickListener {
            finish()
        }
    }

    private fun askCameraPermission() {
        when {
            checkSelfPermission(android.Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            else -> {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "temp_${UUID.randomUUID()}.jpg"
        )

        tempImageFile = photoFile
        productImageTemp = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, productImageTemp)
        cameraLauncher.launch(intent)
    }

    private fun saveFinalImage(): File? {
        if (productImageTemp == null) return null

        return try {
            val inputStream = contentResolver.openInputStream(productImageTemp!!)
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "product_${System.currentTimeMillis()}.jpg"
            )

            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            finalImageFile = file

            tempImageFile?.delete()
            tempImageFile = null

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setTheAdapter() {
        binding.autoTypeCompleteQ1.setAdapter(GetAdapterOfOptionsUseCase.getCategoriesAdapter(this))
    }

    private fun allFieldsDone(): Boolean {
        if (productImageTemp == null) {
            Toast.makeText(this, "Please capture an image", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.autoTypeCompleteQ1.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.productBrandET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a brand", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.buyingPriceET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a selling price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.countET.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter a count", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.countET.text.toString().toInt() <= 0) {
            Toast.makeText(this, "Invalid count", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.buyingPriceET.text.toString().toDouble() <= 0) {
            Toast.makeText(this, "Invalid buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().toDouble() <= 0) {
            Toast.makeText(this, "Invalid selling price", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.sellingPriceET.text.toString().toDouble() <=
            binding.buyingPriceET.text.toString().toDouble()
        ) {
            Toast.makeText(this, "Selling price must be > buying price", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveProduct(product: Product) {
        resetProduct()
        lifecycleScope.launch(Dispatchers.IO) {
            pRepo.insertProduct(product)
        }
        id = IdGenerator.generateProductId()
    }

    private fun resetProduct() {
        product = null
        productImageTemp = null
        tempImageFile = null
        finalImageFile = null
        binding.apply {
            productBrandET.text?.clear()
            buyingPriceET.text?.clear()
            sellingPriceET.text?.clear()
            countET.text?.clear()
            autoTypeCompleteQ1.text?.clear()
            productImg.setImageResource(R.drawable.ic_camera)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (product == null && tempImageFile != null) {
            try {
                if (tempImageFile!!.exists()) tempImageFile!!.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showBarcode() {}
}
