package com.domio.app.features.assets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.domio.app.core.domain.AssetCategories
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAssetScreen(
    onBack: () -> Unit,
    onSave: (
        name: String,
        category: String,
        subCategory: String?,
        brand: String?,
        model: String?,
        barcode: String?,
        serialNumber: String?,
        location: String?,
        room: String?,
        condition: String?,
        notes: String?,
        purchaseDate: Long?,
        purchasePrice: Double?,
        sellerStore: String?,
        warrantyProvider: String?,
        warrantyStartDate: Long?,
        warrantyEndDate: Long?,
        maintenanceCost: Double?,
        repairCost: Double?,
        imageUri: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AssetCategories.CATEGORIES.first().name) }
    var subCategory by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }

    var purchasePriceText by remember { mutableStateOf("") }
    var sellerStore by remember { mutableStateOf("") }
    var warrantyProvider by remember { mutableStateOf("") }

    // Date Pickers
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showWarrantyDatePicker by remember { mutableStateOf(false) }

    val purchaseDatePickerState = rememberDatePickerState()
    val warrantyDatePickerState = rememberDatePickerState()

    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var expandedConditionDropdown by remember { mutableStateOf(false) }

    val conditionsList = listOf("New", "Excellent", "Good", "Fair", "Needs Service")

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        return dateFormatter.format(Date(timestamp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Thing Manually") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSave(
                                name,
                                category,
                                subCategory.ifBlank { null },
                                brand.ifBlank { null },
                                model.ifBlank { null },
                                barcode.ifBlank { null },
                                serialNumber.ifBlank { null },
                                location.ifBlank { null },
                                room.ifBlank { null },
                                condition,
                                notes.ifBlank { null },
                                purchaseDatePickerState.selectedDateMillis,
                                purchasePriceText.toDoubleOrNull(),
                                sellerStore.ifBlank { null },
                                warrantyProvider.ifBlank { null },
                                null,
                                warrantyDatePickerState.selectedDateMillis,
                                null,
                                null,
                                null
                            )
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                text = "Store item specifications, warranty, and location details.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Product Name *") },
                placeholder = { Text("e.g. MacBook Pro M3, Honda City, LG Refrigerator") },
                singleLine = true
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategoryDropdown,
                onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false }
                ) {
                    AssetCategories.CATEGORIES.forEach { catInfo ->
                        DropdownMenuItem(
                            text = { Text("${catInfo.emoji} ${catInfo.name}") },
                            onClick = {
                                category = catInfo.name
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            // Brand & Model
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Brand") },
                    placeholder = { Text("Apple, LG") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Model") },
                    placeholder = { Text("A2992") },
                    singleLine = true
                )
            }

            // Purchase Price & Seller
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Price (₹)") },
                    placeholder = { Text("45000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellerStore,
                    onValueChange = { sellerStore = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Store / Seller") },
                    placeholder = { Text("Amazon, Croma") },
                    singleLine = true
                )
            }

            // Purchase Date
            OutlinedTextField(
                value = formatDate(purchaseDatePickerState.selectedDateMillis),
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Purchase Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showPurchaseDatePicker = true }) {
                        Icon(Icons.Outlined.DateRange, contentDescription = "Select Date")
                    }
                },
                interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) showPurchaseDatePicker = true
                        }
                    }
                }
            )

            // Warranty Expiry Date
            OutlinedTextField(
                value = formatDate(warrantyDatePickerState.selectedDateMillis),
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Warranty Expiry Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showWarrantyDatePicker = true }) {
                        Icon(Icons.Outlined.DateRange, contentDescription = "Select Date")
                    }
                },
                interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) showWarrantyDatePicker = true
                        }
                    }
                }
            )

            // Location & Room
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Location") },
                    placeholder = { Text("Home, Office") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Room") },
                    placeholder = { Text("Living Room") },
                    singleLine = true
                )
            }

            // Serial Number & Barcode
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Serial Number") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Barcode") },
                    singleLine = true
                )
            }

            // Condition Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedConditionDropdown,
                onExpandedChange = { expandedConditionDropdown = !expandedConditionDropdown }
            ) {
                OutlinedTextField(
                    value = condition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Condition") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConditionDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedConditionDropdown,
                    onDismissRequest = { expandedConditionDropdown = false }
                ) {
                    conditionsList.forEach { cond ->
                        DropdownMenuItem(
                            text = { Text(cond) },
                            onClick = {
                                condition = cond
                                expandedConditionDropdown = false
                            }
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showPurchaseDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showPurchaseDatePicker = false },
                confirmButton = { TextButton(onClick = { showPurchaseDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showPurchaseDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = purchaseDatePickerState)
            }
        }

        if (showWarrantyDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showWarrantyDatePicker = false },
                confirmButton = { TextButton(onClick = { showWarrantyDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showWarrantyDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = warrantyDatePickerState)
            }
        }
    }
}