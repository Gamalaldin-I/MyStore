package com.example.htopstore.ui.scan

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.htopstore.databinding.ActivityScanBinding
import com.example.htopstore.util.helper.CameraHelper
import com.example.htopstore.util.helper.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraHelper: CameraHelper
    private var scanLineAnimator: ValueAnimator? = null
    private var fromAdding = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    private val vm: ScanViewModel by viewModels()

    // Gallery launcher for scanning barcodes from images
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    // Here you would implement barcode detection from image
                    // For now, show a toast
                    Toast.makeText(this, "Image selected - implement barcode ${selectedImageUri} detection from image", Toast.LENGTH_LONG).show()
                }
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm.found.observe(this) {
            val color = if (it) {
                resources.getColor(com.example.htopstore.R.color.success, null)
            } else {
                resources.getColor(com.example.htopstore.R.color.error, null)
            }
            binding.cardBg.setBackgroundColor(color)

        }

        fromAdding = intent.getBooleanExtra("fromAdding", false)

        setupCamera()
        observeViewModel()
        setupClickListeners()
        startScanLineAnimation()
    }

    private fun setupCamera() {
        cameraHelper = CameraHelper(
            context = this,
            lifecycleOwner = this,
            previewView = binding.previewView,
            executor = Executors.newSingleThreadExecutor(),
            onBarcodeDetected = { type, value ->
                if (fromAdding) {
                    val processedCode = fixEan13(value)
                    vm.onAddProduct(processedCode)
                    playBeep()
                    cameraHelper.pauseAnalysis()
                    showSuccessResult(processedCode)
                } else {
                    runOnUiThread {
                        val processedCode = fixEan13(value)
                        vm.onScanned(processedCode)
                        playBeep()
                        cameraHelper.pauseAnalysis()
                        showSuccessResult(processedCode)
                    }
                }
            }
        )

        if (PermissionHelper.allPermissionsGranted(this)) {
            cameraHelper.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }



        // Main scan button - Resume scanning
        binding.scanBtn.setOnClickListener {
            resumeScanning()
        }


        // Close result card
        binding.closeResult.setOnClickListener {
            hideResultCard()
            resumeScanning()
        }
    }

    private fun observeViewModel() {
        vm.message.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessResult(code: String) {
        runOnUiThread {
            binding.scannedTV.text = code
            binding.resultCard.visibility = View.VISIBLE

            // Hide instruction text when result is shown
            binding.instructionText.visibility = View.GONE

            // Auto-hide result after delay if not from adding screen
            if (fromAdding) {
                binding.resultCard.postDelayed({
                    if (fromAdding) {
                        finish()
                    }
                }, 2000)
            }
        }
    }

    private fun hideResultCard() {
        binding.resultCard.visibility = View.GONE
        binding.instructionText.visibility = View.VISIBLE
        binding.scannedTV.text = ""
    }

    private fun resumeScanning() {
        hideResultCard()
        cameraHelper.resumeAnalysis()

        // Animate scan button
        binding.scanBtn.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                binding.scanBtn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }





    private fun startScanLineAnimation() {
        scanLineAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                val scanFrame = binding.scanningFrame
                val translationY = (scanFrame.height * value) - (scanFrame.height / 2)
                binding.scanLine.translationY = translationY
            }

            start()
        }
    }

    private fun playBeep() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fixEan13(code: String): String {
        return when {
            code.length < 13 -> code.padStart(13, '0')
            code.length == 13 -> code
            else -> code.take(13) // Take first 13 characters if longer
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (PermissionHelper.allPermissionsGranted(this)) {
                cameraHelper.startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to scan barcodes",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanLineAnimator?.cancel()
        cameraHelper.shutdown()
    }

    override fun onPause() {
        super.onPause()
        scanLineAnimator?.pause()
    }

    override fun onResume() {
        super.onResume()
        scanLineAnimator?.resume()
    }
}