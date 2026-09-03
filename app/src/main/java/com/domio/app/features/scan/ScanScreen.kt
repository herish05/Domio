package com.domio.app.features.scan

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.domio.app.core.ml.SmartParsedDocument
import com.domio.app.core.ml.TextRecognizerHelper
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    onScanProduct: () -> Unit,
    onDocumentScanned: (SmartParsedDocument?) -> Unit
) {
    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val textRecognizer = remember { TextRecognizerHelper(context) }
    var isProcessing by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            
            val imageUri = scanResult?.pages?.firstOrNull()?.imageUri
            
            if (imageUri != null) {
                isProcessing = true
                coroutineScope.launch {
                    val permanentUri = try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        if (inputStream != null) {
                            val documentsDir = java.io.File(context.filesDir, "documents")
                            if (!documentsDir.exists()) documentsDir.mkdirs()
                            
                            val extension = "jpg" // ML Kit returns JPEG or PDF but pages are always JPEG/PNG image uris
                            val file = java.io.File(documentsDir, "doc_${System.currentTimeMillis()}.$extension")
                            val outputStream = java.io.FileOutputStream(file)
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                            outputStream.close()
                            
                            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        } else imageUri
                    } catch (e: Exception) {
                        imageUri
                    }

                    val parsed = textRecognizer.analyzeDocument(permanentUri)
                    isProcessing = false
                    onDocumentScanned(parsed)
                }
            } else {
                // No image found, but maybe PDF? We need an image for OCR.
                onDocumentScanned(SmartParsedDocument(fileUri = ""))
            }
        }
    }

    // Initialize the Document Scanner options
    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scan",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Quickly add things and documents.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------
            // PRODUCT
            // -----------------------------

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scan Product",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan a barcode or QR code to add a product.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onScanProduct,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan product")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------
            // DOCUMENT
            // -----------------------------

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scan Document",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan bills, warranties, insurance and other documents.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scanner.getStartScanIntent(activity)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                }
                                .addOnFailureListener {
                                    // Fallback to manual if scanner is unavailable
                                    onDocumentScanned(null)
                                }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isProcessing) "Processing Document..." else "Scan document")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Manual entry option just in case
                    androidx.compose.material3.TextButton(
                        onClick = { onDocumentScanned(null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Text("Add manually")
                    }
                }
            }
        }
    }
}