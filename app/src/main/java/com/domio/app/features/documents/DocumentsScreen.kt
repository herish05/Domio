package com.domio.app.features.documents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.domio.app.core.ml.SmartParsedDocument
import com.domio.app.core.ml.TextRecognizerHelper
import com.domio.app.core.util.DocumentConverter
import com.domio.app.core.util.ExportAction
import com.domio.app.core.util.ExportFormat
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.ui.components.ThemeToggleIconButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<DocumentEntity>,
    onAdd: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onScanDocument: () -> Unit = {},
    onDocumentScannedFromGallery: (SmartParsedDocument?) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val textRecognizer = remember { TextRecognizerHelper(context) }

    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isProcessingImport by remember { mutableStateOf(false) }

    var selectedExportDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // FILE PICKER FOR IMPORT FROM GALLERY / STORAGE
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingImport = true
            coroutineScope.launch {
                val permanentUri = try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val docsDir = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }
                        val ext = if (context.contentResolver.getType(uri)?.contains("pdf", true) == true) "pdf" else "jpg"
                        val file = File(docsDir, "doc_${System.currentTimeMillis()}.$ext")
                        val outputStream = FileOutputStream(file)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()

                        Uri.fromFile(file)
                    } else uri
                } catch (e: Exception) {
                    uri
                }

                val parsed = textRecognizer.analyzeDocument(permanentUri)
                isProcessingImport = false
                onDocumentScannedFromGallery(parsed)
            }
        }
    }

    if (selectedExportDoc != null) {
        ExportFormatDialog(
            initialAction = ExportAction.SHARE,
            onDismiss = { selectedExportDoc = null },
            onConfirm = { format, action ->
                val doc = selectedExportDoc
                selectedExportDoc = null
                if (doc != null) {
                    DocumentConverter.exportDocument(context, doc, format, action)
                }
            }
        )
    }

    val filteredDocuments = if (search.isBlank()) {
        documents
    } else {
        documents.filter { document ->
            document.title.contains(search, ignoreCase = true) ||
            document.type.contains(search, ignoreCase = true) ||
            document.issuer.orEmpty().contains(search, ignoreCase = true)
        }
    }

    val totalAmount = remember(documents) {
        documents.mapNotNull { it.amount }.sum()
    }
    val warrantyCount = remember(documents) {
        documents.count { it.type.contains("Warranty", true) }
    }

    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Document Options")
                }

                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("📷 Scan Document with Camera", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showFabMenu = false
                            onScanDocument()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📥 Import PDF / Image from Gallery", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showFabMenu = false
                            filePickerLauncher.launch("*/*")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("✍️ Add Document Manually", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showFabMenu = false
                            onAdd()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // TOP HEADER WITH THEME TOGGLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedCategory == null) {
                        Column {
                            Text(
                                text = "Document Vault 📄",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Centralized storage for bills, warranties & IDs",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedCategory = null }, modifier = Modifier.offset(x = (-12).dp)) {
                                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = if (selectedCategory == "All") "All Documents" else selectedCategory ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ThemeToggleIconButton()
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STORAGE SUMMARY & QUICK ACTION BAR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${documents.size} Documents Saved",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total Value: ₹${totalAmount.toInt()} • $warrantyCount Warranties",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // QUICK ACTION BUTTONS (SCAN CAMERA, GALLERY IMPORT, MANUAL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onScanDocument,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan Camera", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                enabled = !isProcessingImport,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                if (isProcessingImport) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Outlined.FileUpload, null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isProcessingImport) "Importing..." else "Import File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    placeholder = { Text("Search documents by name, store, type...") },
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (filteredDocuments.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "No documents found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Scan receipts or upload PDF/images to keep them organized.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (selectedCategory == null && search.isBlank()) {
                    // Show Category Grid
                    val categories = documents.map { it.type.ifBlank { "Other" } }.distinct().sorted()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            CategoryCard(
                                name = "All",
                                count = documents.size,
                                onClick = { selectedCategory = "All" }
                            )
                        }
                        items(categories) { category ->
                            val count = documents.count { (it.type.ifBlank { "Other" }) == category }
                            CategoryCard(
                                name = category,
                                count = count,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                } else {
                    // Show list of documents
                    val displayList = if (selectedCategory == "All" || search.isNotBlank()) {
                        filteredDocuments
                    } else {
                        filteredDocuments.filter { (it.type.ifBlank { "Other" }) == selectedCategory }
                    }

                    if (displayList.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No documents in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(items = displayList, key = { it.id }) { document ->
                                DocumentCard(
                                    document = document,
                                    onClick = { onDocumentClick(document.id) },
                                    onExportClick = { selectedExportDoc = document }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    name: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        name == "All" -> Icons.Outlined.Folder
                        name.contains("ID", true) -> Icons.Outlined.Badge
                        name.contains("Insurance", true) -> Icons.Outlined.HealthAndSafety
                        name.contains("Invoice", true) || name.contains("Receipt", true) -> Icons.Outlined.Receipt
                        name.contains("Warranty", true) -> Icons.Outlined.VerifiedUser
                        else -> Icons.Outlined.Description
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column {
                Text(
                    text = if (name == "All") "All Documents" else name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        document.type.contains("Warranty", true) -> Icons.Outlined.VerifiedUser
                        document.type.contains("Receipt", true) || document.type.contains("Bill", true) -> Icons.Outlined.Receipt
                        else -> Icons.Outlined.Description
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOfNotNull(document.type.ifBlank { null }, document.issuer).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (document.issueDate != null && document.issueDate > 0) {
                    val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(document.issueDate))
                    Text(
                        text = "📅 $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (document.amount != null && document.amount > 0) {
                    Text(
                        text = "₹${document.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onExportClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Export & Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}