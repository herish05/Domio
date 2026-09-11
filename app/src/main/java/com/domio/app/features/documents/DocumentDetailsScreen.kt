package com.domio.app.features.documents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.domio.app.core.util.DocumentConverter
import com.domio.app.core.util.ExportAction
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

    var showExportDialog by remember { mutableStateOf(false) }
    var exportInitialAction by remember { mutableStateOf(ExportAction.SHARE) }
    var showViewerDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    LaunchedEffect(documentId) {
        document = viewModel.getDocument(documentId)
    }

    if (showExportDialog && document != null) {
        ExportFormatDialog(
            initialAction = exportInitialAction,
            onDismiss = { showExportDialog = false },
            onConfirm = { format, action ->
                showExportDialog = false
                DocumentConverter.exportDocument(context, document!!, format, action)
            }
        )
    }

    if (showViewerDialog && document != null && document!!.fileUri.isNotBlank()) {
        DocumentViewerDialog(
            fileUriString = document!!.fileUri,
            title = document!!.title,
            onDismiss = { showViewerDialog = false }
        )
    }

    if (showDeleteDialog && document != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteConfirmText = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Document?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This action is permanent and cannot be undone. To prevent accidental deletion, please type DELETE below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type DELETE to confirm") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = document!!.id
                        showDeleteDialog = false
                        deleteConfirmText = ""
                        viewModel.deleteDocument(idToDelete)
                        android.widget.Toast.makeText(context, "Document deleted", android.widget.Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    enabled = deleteConfirmText.trim() == "DELETE",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteConfirmText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "Document Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (document != null) {
                        IconButton(onClick = {
                            exportInitialAction = ExportAction.SHARE
                            showExportDialog = true
                        }) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share Document")
                        }

                        IconButton(onClick = {
                            exportInitialAction = ExportAction.DOWNLOAD
                            showExportDialog = true
                        }) {
                            Icon(imageVector = Icons.Outlined.Download, contentDescription = "Download Document")
                        }

                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Document",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (document?.fileUri?.isNotBlank() == true) {
                ExtendedFloatingActionButton(
                    onClick = { showViewerDialog = true },
                    icon = { Icon(Icons.Outlined.OpenInNew, contentDescription = "Preview File") },
                    text = { Text("Preview In-App", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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

            // INLINE FILE PREVIEW THUMBNAIL CARD
            if (doc.fileUri.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { showViewerDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isPdf = remember(doc.fileUri) { DocumentConverter.isPdfUri(context, doc.fileUri) }
                        if (isPdf) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PDF Document Attached",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Tap to open interactive viewer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val bitmap = remember(doc.fileUri) {
                                try {
                                    context.contentResolver.openInputStream(doc.fileUri.toUri())?.use {
                                        android.graphics.BitmapFactory.decodeStream(it)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Document Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f))
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.OpenInNew, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tap to View Fullscreen", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ACTION BUTTONS ROW (SHARE & DOWNLOAD FORMAT CONVERTER)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        exportInitialAction = ExportAction.SHARE
                        showExportDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        exportInitialAction = ExportAction.DOWNLOAD
                        showExportDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()

            // METADATA DETAILS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = doc.type,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    val renderedKeys = mutableSetOf<String>()

                    // Display Primary Number & Issuer if present
                    if (!doc.documentNumber.isNullOrBlank()) {
                        DetailRow("Document Number", doc.documentNumber)
                        renderedKeys.add("document number")
                    }
                    if (!doc.issuer.isNullOrBlank()) {
                        DetailRow("Issued By / Store", doc.issuer)
                        renderedKeys.add("issued by")
                        renderedKeys.add("store name")
                    }
                    if (doc.issueDate != null) {
                        DetailRow("Issue Date", dateFormatter.format(Date(doc.issueDate)))
                    }
                    if (doc.expiryDate != null) {
                        DetailRow("Expiry Date", dateFormatter.format(Date(doc.expiryDate)))
                    }
                    if (doc.amount != null) {
                        DetailRow("Total Amount", "₹${doc.amount.toInt()}")
                    }

                    // Display all Extracted Dynamic Key-Value Fields
                    if (!doc.dynamicFields.isNullOrBlank()) {
                        var parsedFields: Map<String, String>? = null
                        try {
                            val mapType = object : TypeToken<Map<String, String>>() {}.type
                            parsedFields = Gson().fromJson(doc.dynamicFields, mapType)
                        } catch (_: Exception) {}

                        parsedFields?.filterValues { it.isNotBlank() }?.forEach { (key, value) ->
                            if (!renderedKeys.contains(key.lowercase())) {
                                DetailRow(key, value)
                            }
                        }
                    }

                    if (!doc.notes.isNullOrBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        DetailRow("Notes", doc.notes)
                    }
                }
            }

            // Bottom padding for FAB
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
