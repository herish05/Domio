package com.domio.app.features.scan.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.domio.app.features.scan.BarcodeResult
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
class BarcodeAnalyzer(
    private val onBarcodeDetected: (BarcodeResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner =
        BarcodeScanning.getClient()

    private var detected = false

    override fun analyze(imageProxy: ImageProxy) {
        android.util.Log.d(
            "DOMIO_BARCODE",
            "Analyzing camera frame"
        )

        if (detected) {
            imageProxy.close()
            return
        }



        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->

                if (detected) {
                    return@addOnSuccessListener
                }

                val barcode =
                    barcodes.firstOrNull()

                val value =
                    barcode?.rawValue

                if (!value.isNullOrBlank()) {

                    android.util.Log.d(
                        "DOMIO_BARCODE",
                        "Barcode detected: $value"
                    )

                    detected = true

                    onBarcodeDetected(
                        BarcodeResult(
                            value = value,
                            format =
                                barcode.format.toString()
                        )
                    )
                }
            }
            .addOnFailureListener { error ->

                println(
                    "Barcode scanning failed: ${error.message}"
                )
            }
            .addOnCompleteListener {

                imageProxy.close()
            }
    }

    fun close() {
        scanner.close()
    }
}