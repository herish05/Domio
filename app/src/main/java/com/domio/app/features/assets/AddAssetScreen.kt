package com.domio.app.features.assets

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.core.ml.SmartParsedDocument
import com.domio.app.core.ml.TextRecognizerHelper
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

@Composable
fun AddAssetScreen(
    onBack: () -> Unit,
    onScanProduct: () -> Unit,
    onScanBillResult: (SmartParsedDocument) -> Unit,
    onManual: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val textRecognizer = remember { TextRecognizerHelper(context) }
    var isProcessingBill by remember { mutableStateOf(false) }

    val billScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val imageUri = scanResult?.pages?.firstOrNull()?.imageUri

            if (imageUri != null) {
                isProcessingBill = true
                coroutineScope.launch {
                    val permanentUri = try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        if (inputStream != null) {
                            val documentsDir = java.io.File(context.filesDir, "documents")
                            if (!documentsDir.exists()) documentsDir.mkdirs()

                            val file = java.io.File(documentsDir, "doc_${System.currentTimeMillis()}.jpg")
                            val outputStream = java.io.FileOutputStream(file)
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                            outputStream.close()

                            androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                        } else imageUri
                    } catch (e: Exception) {
                        imageUri
                    }

                    val parsed = textRecognizer.analyzeDocument(permanentUri)
                    isProcessingBill = false
                    onScanBillResult(parsed)
                }
            }
        }
    }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Add something",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "How would you like to add it?",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        AddAssetOption(
            icon = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
            title = "Scan product",
            description = "Scan a barcode, QR code or product label",
            onClick = onScanProduct,
            enabled = !isProcessingBill
        )

        Spacer(modifier = Modifier.height(14.dp))

        AddAssetOption(
            icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
            title = if (isProcessingBill) "Processing bill..." else "Scan bill",
            description = "Extract product and purchase details from a bill using AI",
            onClick = {
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        billScannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener {
                        onManual()
                    }
            },
            isLoading = isProcessingBill,
            enabled = !isProcessingBill
        )

        Spacer(modifier = Modifier.height(14.dp))

        AddAssetOption(
            icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            title = "Add manually",
            description = "Enter the product details yourself",
            onClick = onManual,
            enabled = !isProcessingBill
        )
    }
}

@Composable
private fun AddAssetOption(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icon()
                Text(text = title, fontSize = 18.sp)
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}