package com.domio.app.features.documents

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.domio.app.core.ml.SmartParsedDocument
import com.domio.app.core.ml.TextRecognizerHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    initialData: SmartParsedDocument? = null,
    onBack: () -> Unit,
    onSave: (
        title: String,
        type: String,
        number: String?,
        issuer: String?,
        issueDate: Long?,
        expiryDate: Long?,
        amount: Double?,
        notes: String?,
        reminderEnabled: Boolean,
        reminderDaysBefore: Int,
        fileUri: String,
        dynamicFieldsJson: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val textRecognizer = remember { TextRecognizerHelper(context) }

    var currentFileUri by remember(initialData) { mutableStateOf(initialData?.fileUri ?: "") }
    var isAnalyzingFile by remember { mutableStateOf(false) }

    var title by remember(initialData) { mutableStateOf(initialData?.extractedTitle ?: "") }
    var type by remember(initialData) { mutableStateOf(initialData?.extractedType ?: "") }
    var number by remember(initialData) { mutableStateOf(initialData?.extractedDocumentNumber ?: "") }
    var issuer by remember(initialData) { mutableStateOf(initialData?.extractedIssuer ?: "") }
    var amount by remember(initialData) { mutableStateOf(initialData?.extractedAmount?.toString() ?: "") }
    
    var dynamicFields by remember(initialData) {
        val initialMap = mutableListOf<Pair<String, String>>()
        initialData?.extractedDynamicFields?.forEach { (k, v) ->
            if (v.isNotBlank()) initialMap.add(Pair(k, v))
        }
        mutableStateOf(initialMap.toList())
    }
    
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderDaysBefore by remember { mutableStateOf("30") }

    // Date Picker States
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    
    val issueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialData?.extractedDate)
    val expiryDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialData?.extractedExpiryDate)

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        return dateFormatter.format(Date(timestamp))
    }

    // GALLERY / FILE PICKER LAUNCHER
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isAnalyzingFile = true
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

                currentFileUri = permanentUri.toString()

                // Run ML OCR if image or valid format
                val parsed = textRecognizer.analyzeDocument(permanentUri)
                if (parsed != null) {
                    parsed.extractedTitle?.takeIf { it.isNotBlank() }?.let { if (title.isBlank()) title = it }
                    parsed.extractedType?.takeIf { it.isNotBlank() }?.let { if (type.isBlank()) type = it }
                    parsed.extractedIssuer?.takeIf { it.isNotBlank() }?.let { if (issuer.isBlank()) issuer = it }
                    parsed.extractedDocumentNumber?.takeIf { it.isNotBlank() }?.let { if (number.isBlank()) number = it }
                    if (parsed.extractedAmount != null && amount.isBlank()) amount = parsed.extractedAmount.toString()
                    
                    val newDynamic = mutableListOf<Pair<String, String>>()
                    parsed.extractedDynamicFields?.forEach { (k, v) ->
                        if (v.isNotBlank()) newDynamic.add(Pair(k, v))
                    }
                    if (parsed.extractedDocumentNumber?.isNotBlank() == true && newDynamic.none { it.first.contains("Number", true) || it.first.contains("No", true) }) {
                        newDynamic.add(0, Pair("Document Number", parsed.extractedDocumentNumber))
                    }
                    if (parsed.extractedIssuer?.isNotBlank() == true && newDynamic.none { it.first.contains("Issuer", true) || it.first.contains("Store", true) }) {
                        newDynamic.add(Pair("Issued By", parsed.extractedIssuer))
                    }
                    if (parsed.extractedAmount != null && newDynamic.none { it.first.contains("Amount", true) || it.first.contains("Price", true) }) {
                        newDynamic.add(Pair("Total Amount", "₹${parsed.extractedAmount}"))
                    }
                    dynamicFields = newDynamic
                }
                isAnalyzingFile = false
            }
        }
    }

    // Dynamic Fields Logic
    val isIdCard = type.contains("ID", ignoreCase = true) || 
                   type.contains("Card", ignoreCase = true) || 
                   type.contains("Passport", ignoreCase = true) || 
                   type.contains("Licence", ignoreCase = true)
                   
    val isInvoice = type.contains("Invoice", ignoreCase = true) || 
                    type.contains("Receipt", ignoreCase = true)

    val showAmount = !isIdCard
    val showExpiry = !isInvoice
    val showIssuer = !isInvoice
    val showReminder = !isInvoice

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Document", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val cleanDynamicMap = dynamicFields.filter { it.first.isNotBlank() && it.second.isNotBlank() }.toMap()
                            val jsonFields = if (cleanDynamicMap.isNotEmpty()) {
                                com.google.gson.Gson().toJson(cleanDynamicMap)
                            } else null
                            
                            val resolvedNumber = number.ifBlank {
                                cleanDynamicMap.entries.firstOrNull { it.key.contains("Number", true) || it.key.contains("No", true) }?.value
                            }
                            val resolvedIssuer = if (showIssuer) issuer.ifBlank {
                                cleanDynamicMap.entries.firstOrNull { it.key.contains("Issuer", true) || it.key.contains("Store", true) }?.value
                            } else null
                            
                            val resolvedAmount = if (showAmount) {
                                amount.toDoubleOrNull() ?: cleanDynamicMap.entries.firstOrNull { it.key.contains("Amount", true) || it.key.contains("Price", true) }?.value?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()
                            } else null

                            val doSave = {
                                onSave(
                                    title,
                                    type,
                                    resolvedNumber,
                                    resolvedIssuer,
                                    issueDatePickerState.selectedDateMillis,
                                    if (showExpiry) expiryDatePickerState.selectedDateMillis else null,
                                    resolvedAmount,
                                    notes.ifBlank { null },
                                    if (showReminder) reminderEnabled else false,
                                    reminderDaysBefore.toIntOrNull() ?: 30,
                                    currentFileUri,
                                    jsonFields
                                )
                            }

                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                com.domio.app.core.ads.AdManager.showInterstitialAd(activity, doSave)
                            } else {
                                doSave()
                            }
                        },
                        enabled = title.isNotBlank() && type.isNotBlank() && !isAnalyzingFile
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // FILE ATTACHMENT / GALLERY IMPORT BANNER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentFileUri.isNotBlank()) "📎 File Attached" else "📄 Upload Document File",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentFileUri.isNotBlank()) "Tap to replace with another file from gallery" else "Import PDF, PNG, or JPG from device storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        enabled = !isAnalyzingFile,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAnalyzingFile) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentFileUri.isNotBlank()) "Change" else "Browse")
                    }
                }
            }
            
            if (initialData != null) {
                Text(
                    text = "✨ Fields auto-filled from document scan.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val requiredLabel: @Composable (String) -> Unit = { text ->
                Text(
                    buildAnnotatedString {
                        append(text)
                        withStyle(SpanStyle(color = Color.Red)) { append(" *") }
                    }
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { requiredLabel("Document name") },
                placeholder = { Text("Aadhaar Card / PAN Card / Electricity Bill") },
                singleLine = true
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                modifier = Modifier.fillMaxWidth(),
                label = { requiredLabel("Document type") },
                placeholder = { Text("ID Document / Utility Bill / Receipt / Vehicle Document") },
                singleLine = true
            )

            // DYNAMIC & EXTRACTED FIELDS CONTAINER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Extracted Document Fields",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Runtime key-value fields detected from document.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                dynamicFields = dynamicFields + Pair("", "")
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add Field", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Field", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (dynamicFields.isEmpty()) {
                        Text(
                            text = "No dynamic fields added yet. Tap '+ Add Field' to create custom columns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        dynamicFields.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = pair.first,
                                    onValueChange = { newKey ->
                                        val updated = dynamicFields.toMutableList()
                                        updated[index] = Pair(newKey, pair.second)
                                        dynamicFields = updated
                                    },
                                    modifier = Modifier.weight(0.44f),
                                    label = { Text("Field Label") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = pair.second,
                                    onValueChange = { newValue ->
                                        val updated = dynamicFields.toMutableList()
                                        updated[index] = Pair(pair.first, newValue)
                                        dynamicFields = updated
                                    },
                                    modifier = Modifier.weight(0.56f),
                                    label = { Text("Value") },
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        val updated = dynamicFields.toMutableList()
                                        updated.removeAt(index)
                                        dynamicFields = updated
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete Field",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // OPTIONAL DATE & NOTES SECTION
            OutlinedTextField(
                value = formatDate(issueDatePickerState.selectedDateMillis),
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Issue date (Optional)") },
                placeholder = { Text("DD/MM/YYYY") },
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showIssueDatePicker = true }) {
                        Icon(imageVector = Icons.Outlined.DateRange, contentDescription = "Select Date")
                    }
                },
                interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                showIssueDatePicker = true
                            }
                        }
                    }
                }
            )

            if (showExpiry) {
                OutlinedTextField(
                    value = formatDate(expiryDatePickerState.selectedDateMillis),
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Expiry date (Optional)") },
                    placeholder = { Text("DD/MM/YYYY") },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showExpiryDatePicker = true }) {
                            Icon(imageVector = Icons.Outlined.DateRange, contentDescription = "Select Date")
                        }
                    },
                    interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showExpiryDatePicker = true
                                }
                            }
                        }
                    }
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (Optional)") },
                minLines = 2
            )

            if (showReminder && showExpiry) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expiry reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Remind me before this document expires.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (reminderEnabled) {
                    OutlinedTextField(
                        value = reminderDaysBefore,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                reminderDaysBefore = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Remind me days before") },
                        placeholder = { Text("30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showIssueDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showIssueDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showIssueDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showIssueDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = issueDatePickerState)
            }
        }

        if (showExpiryDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showExpiryDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showExpiryDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExpiryDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = expiryDatePickerState)
            }
        }
    }
}