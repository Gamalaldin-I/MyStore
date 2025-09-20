package com.example.htopstore.util.helper

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

class CameraHelper (
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val executor: ExecutorService,
    private val onBarcodeDetected: (String, String) -> Unit
    ) {

        private lateinit var analyzer: CodeAnalyser

        fun startCamera() {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                analyzer = CodeAnalyser(onBarcodeDetected)

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, analyzer)
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, analysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraX", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        fun pauseAnalysis() {
            if (::analyzer.isInitialized) analyzer.pauseAnalysis()
        }

        fun resumeAnalysis() {
            if (::analyzer.isInitialized) analyzer.resumeAnalysis()
        }

        fun shutdown() {
            executor.shutdown()
        }
    }