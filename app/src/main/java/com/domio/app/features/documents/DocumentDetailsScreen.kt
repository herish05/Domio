package com.domio.app.features.documents

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.domio.app.data.local.entity.DocumentEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailsScreen(
    documentId: String,
    viewModel: DocumentViewModel,
    onBack: () -> Unit
) {
    var document by remember { mutableStateOf<DocumentEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(documentId) {
        document = viewModel.getDocument(documentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "Document Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (document?.fileUri?.isNotBlank() == true) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Outlined.Share, null) },
                                onClick = {
                                    expanded = false
                                    val uri = document!!.fileUri.toUri()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save to Gallery") },
                                onClick = {
                                    expanded = false
                                    try {
                                        val uri = document!!.fileUri.toUri()
                                        val resolver = context.contentResolver
                                        
                                        val contentValues = android.content.ContentValues().apply {
                                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Domio_${System.currentTimeMillis()}.jpg")
                                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Domio")
                                            }
                                        }
                                        
                                        val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                        if (imageUri != null) {
                                            resolver.openOutputStream(imageUri)?.use { outStream ->
                                                resolver.openInputStream(uri)?.use { inStream ->
                                                    inStream.copyTo(outStream)
                                                }
                                            }
                                            android.widget.Toast.makeText(context, "Saved to Gallery (Pictures/Domio)", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to create file", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Print") },
                                onClick = {
                                    expanded = false
                                    try {
                                        val uri = document!!.fileUri.toUri()
                                        androidx.print.PrintHelper(context).apply {
                                            scaleMode = androidx.print.PrintHelper.SCALE_MODE_FIT
                                        }.printBitmap("Domio Document", uri)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Failed to print", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (document?.fileUri?.isNotBlank() == true) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val uri = document!!.fileUri.toUri()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "image/*") // ML Kit scanner returns images/PDFs. Use generic image for now or parse mime.
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback if no viewer
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(fallbackIntent)
                        }
                    },
                    icon = { Icon(Icons.Outlined.OpenInNew, contentDescription = "View File") },
                    text = { Text("View File") }
                )
            }
        }
    ) { paddingValues ->
        val doc = document ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            
            DetailRow("Document Type", doc.type)
            if (!doc.documentNumber.isNullOrBlank()) {
                DetailRow("Document Number", doc.documentNumber)
            }
            if (!doc.issuer.isNullOrBlank()) {
                DetailRow("Issuer", doc.issuer)
            }
            if (doc.issueDate != null) {
                DetailRow("Issue Date", dateFormatter.format(Date(doc.issueDate)))
            }
            if (doc.expiryDate != null) {
                DetailRow("Expiry Date", dateFormatter.format(Date(doc.expiryDate)))
            }
            if (doc.amount != null) {
                DetailRow("Amount", doc.amount.toString())
            }

            if (!doc.dynamicFields.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Extracted Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                var parsedFields: Map<String, String>? = null
                try {
                    val mapType = object : TypeToken<Map<String, String>>() {}.type
                    parsedFields = Gson().fromJson(doc.dynamicFields, mapType)
                } catch (e: Exception) {
                    // Ignore parse errors
                }
                
                parsedFields?.forEach { (key, value) ->
                    if (value.isNotBlank()) {
                        DetailRow(key, value)
                    }
                }
            }

            if (!doc.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Notes", doc.notes)
            }
            
            // Padding for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
