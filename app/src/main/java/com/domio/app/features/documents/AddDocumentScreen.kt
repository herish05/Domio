package com.domio.app.features.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.domio.app.core.ml.SmartParsedDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction

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
    var title by remember(initialData) { mutableStateOf(initialData?.extractedTitle ?: "") }
    var type by remember(initialData) { mutableStateOf(initialData?.extractedType ?: "") }
    var number by remember(initialData) { mutableStateOf(initialData?.extractedDocumentNumber ?: "") }
    
    var issuer by remember(initialData) { mutableStateOf(initialData?.extractedIssuer ?: "") }
    var amount by remember(initialData) { mutableStateOf(initialData?.extractedAmount?.toString() ?: "") }
    
    // Convert extracted map to a mutable list of pairs for editing
    var dynamicFields by remember(initialData) {
        mutableStateOf(
            initialData?.extractedDynamicFields?.toList() ?: emptyList()
        )
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

    // Dynamic Fields Logic
    val isIdCard = type.contains("ID", ignoreCase = true) || 
                   type.contains("Card", ignoreCase = true) || 
                   type.contains("Passport", ignoreCase = true) || 
                   type.contains("Licence", ignoreCase = true)
                   
    val isInvoice = type.contains("Invoice", ignoreCase = true) || 
                    type.contains("Receipt", ignoreCase = true)
                    
    val isWarranty = type.contains("Warranty", ignoreCase = true) || 
                     type.contains("Guarantee", ignoreCase = true)

    val showAmount = !isIdCard
    val showExpiry = !isInvoice
    val showIssuer = !isInvoice
    val showReminder = !isInvoice

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add document") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val jsonFields = if (dynamicFields.isNotEmpty()) {
                                com.google.gson.Gson().toJson(dynamicFields.toMap())
                            } else null
                            
                            onSave(
                                title,
                                type,
                                number.ifBlank { null },
                                if (showIssuer) issuer.ifBlank { null } else null,
                                issueDatePickerState.selectedDateMillis,
                                if (showExpiry) expiryDatePickerState.selectedDateMillis else null,
                                if (showAmount) amount.toDoubleOrNull() else null,
                                notes.ifBlank { null },
                                if (showReminder) reminderEnabled else false,
                                reminderDaysBefore.toIntOrNull() ?: 30,
                                initialData?.fileUri ?: "",
                                jsonFields
                            )
                        },
                        enabled = title.isNotBlank() && type.isNotBlank()
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                        Text("Save")
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

            Text(
                text = "Store important document details in Domio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (initialData != null) {
                Text(
                    text = "✨ Fields auto-filled from your scan.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                placeholder = { Text("Car Insurance") },
                singleLine = true
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                modifier = Modifier.fillMaxWidth(),
                label = { requiredLabel("Document type") },
                placeholder = { Text("Insurance") },
                singleLine = true,
                supportingText = { Text("Changes which fields are shown below.") }
            )

            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Document number") },
                placeholder = { Text("Policy / Invoice / Certificate number") },
                singleLine = true
            )

            if (showIssuer) {
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Issued by") },
                    placeholder = { Text("Government / Company name") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = formatDate(issueDatePickerState.selectedDateMillis),
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Issue date") },
                placeholder = { Text("DD/MM/YYYY") },
                singleLine = true,
                readOnly = true,
                supportingText = { Text("Optional") },
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
                    label = { Text("Expiry date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    singleLine = true,
                    readOnly = true,
                    supportingText = { Text("Leave empty if this document does not expire.") },
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

            if (showAmount) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            if (dynamicFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Document Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                dynamicFields.forEachIndexed { index, pair ->
                    OutlinedTextField(
                        value = pair.second,
                        onValueChange = { newValue ->
                            val updatedList = dynamicFields.toMutableList()
                            updatedList[index] = pair.copy(second = newValue)
                            dynamicFields = updatedList
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(pair.first) },
                        singleLine = true
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3
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
                            style = MaterialTheme.typography.titleMedium
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