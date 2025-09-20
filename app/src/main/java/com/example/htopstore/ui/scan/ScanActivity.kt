package com.example.htopstore.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
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

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }
    private val vm: ScanViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeMessage()


        cameraHelper = CameraHelper(
            context = this,
            lifecycleOwner = this,
            previewView = binding.previewView,
            executor = Executors.newSingleThreadExecutor(),
            onBarcodeDetected = { type, value ->
                runOnUiThread {
                    val prob = fixEan13(value)
                    vm.onScanned(prob)
                    playBeep()

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

    private fun observeMessage() {
        vm.message.observe(this) {
            binding.scannedTV.text = it
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraHelper.shutdown()
    }
}
