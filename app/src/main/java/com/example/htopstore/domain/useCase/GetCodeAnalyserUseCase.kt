package com.example.htopstore.domain.useCase
    import android.util.Log
    import androidx.camera.core.ExperimentalGetImage
    import androidx.camera.core.ImageAnalysis
    import androidx.camera.core.ImageProxy
    import com.google.mlkit.vision.barcode.BarcodeScannerOptions
    import com.google.mlkit.vision.barcode.BarcodeScanning
    import com.google.mlkit.vision.barcode.common.Barcode
    import com.google.mlkit.vision.common.InputImage
    import java.util.concurrent.atomic.AtomicBoolean

class GetCodeAnalyserUseCase(
    private val onBarcodeDetected: (String, String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    // Flag للتحكم في التفعيل/الإيقاف
    private val isActive = AtomicBoolean(true)

    fun pauseAnalysis() {
        isActive.set(false)
    }

    fun resumeAnalysis() {
        isActive.set(true)
    }

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isActive.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage =
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: "null"
                    val type = when (barcode.format) {
                        Barcode.FORMAT_QR_CODE -> "QR Code"
                        Barcode.FORMAT_EAN_13 -> "EAN-13"
                        Barcode.FORMAT_UPC_A -> "UPC-A"
                        Barcode.FORMAT_UPC_E -> "UPC-E"
                        Barcode.FORMAT_CODE_128 -> "Code-128"
                        Barcode.FORMAT_CODE_39 -> "Code-39"
                        else -> "Other"
                    }

                    Log.d("BarcodeScanner", "Detected [$type]: $rawValue")

                    // بعد أول قراءة نوقف التحليل
                    pauseAnalysis()

                    onBarcodeDetected(type, rawValue)
                    break
                }
            }
            .addOnFailureListener {
                Log.e("BarcodeScanner", "Error: ${it.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

