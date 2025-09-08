package com.example.htopstore.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.htopstore.databinding.ActivityScanBinding
import com.example.htopstore.domain.useCase.GetCameraHelperUseCase
import com.example.htopstore.util.PermissionHelper
import java.util.concurrent.Executors
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.R
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.util.CartHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraHelper: GetCameraHelperUseCase
    private lateinit var ProductRepo: ProductRepoImp
    private var products: List<Product> = emptyList()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ProductRepo = ProductRepoImp(this)
        getAllProducts()

        cameraHelper = GetCameraHelperUseCase(
            context = this,
            lifecycleOwner = this,
            previewView = binding.previewView,
            executor = Executors.newSingleThreadExecutor(),
            onBarcodeDetected = { type, value ->
                runOnUiThread {
                    val prob = fixEan13(value)
                    onGetID(prob)
                    playBeep()
                    //make sound beeb

                    cameraHelper.pauseAnalysis()
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

        binding.scanBtn1.setOnClickListener {
            binding.scanBtn1.animate().scaleY(1.3f).scaleX(1.3f).setDuration(100).withEndAction {
                binding.scanBtn1.animate().scaleY(1f).scaleX(1f).duration = 100
                cameraHelper.resumeAnalysis()
            }.start()
        }


    }

     fun playBeep() {
         val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
         toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200) // 300ms
     }
     fun fixEan13(code: String): String {
         return when {
             code.length < 13 -> code.padStart(13, '0')
             code.length == 13 -> code
             else -> throw IllegalArgumentException("الكود مش EAN-13 صالح: $code")
         }
     }
     private fun getAllProducts(){
         lifecycleScope.launch(Dispatchers.IO) {
             products = ProductRepo.getAllProducts()
         }
     }
    @SuppressLint("SetTextI18n")
    private fun onGetID(scannedId:String){
        val product = products.find { it.id == scannedId }

        if (product != null) {
            CartHelper.addToTheCartList(product)
            binding.scannedTV.text = "Scanned: ${product.name} (${product.category})"
            binding.scannedTV.setTextColor(
                resources.getColor(
                    R.color.white,
                    theme
                )
            )
        } else {
            binding.scannedTV.text = "Scanned: $scannedId"
            binding.scannedTV.setTextColor(
                resources.getColor(
                    R.color.dark,
                    theme))
        }
    }




     override fun onDestroy() {
        super.onDestroy()
        cameraHelper.shutdown()
    }
}
