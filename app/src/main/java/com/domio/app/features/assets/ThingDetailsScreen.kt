package com.domio.app.features.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.core.domain.AssetCategories
import com.domio.app.core.domain.WarrantyStatus
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThingDetailsScreen(
    assetId: String,
    viewModel: AssetViewModel,
    onBack: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    var asset by remember { mutableStateOf<AssetEntity?>(null) }
    val linkedDocuments by viewModel.getDocumentsForAsset(assetId).collectAsState(initial = emptyList())
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(assetId) {
        asset = viewModel.getAsset(assetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(asset?.name ?: "Thing Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Archive Thing") },
                            leadingIcon = { Icon(Icons.Outlined.Archive, null) },
                            onClick = {
                                showMenu = false
                                asset?.let {
                                    viewModel.archiveAsset(it.id)
                                    onBack()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Thing", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                asset?.let {
                                    viewModel.deleteAsset(it)
                                    onBack()
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val item = asset ?: return@Scaffold

        val emoji = AssetCategories.getCategoryEmoji(item.category)
        val warrantyStatus = AssetCategories.calculateWarrantyStatus(item.warrantyEndDate)
        val daysRemaining = AssetCategories.getDaysRemaining(item.warrantyEndDate)

        val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─────────────────────────────
            // HEADER CARD
            // ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (!item.brand.isNullOrBlank() || !item.model.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = listOfNotNull(item.brand, item.model).joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text(item.category) }
                        )

                        if (!item.location.isNullOrBlank()) {
                            SuggestionChip(
                                onClick = { },
                                label = { Text("📍 ${item.location}") }
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────
            // WARRANTY CARD
            // ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Warranty Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val (statusText, statusColor) = when (warrantyStatus) {
                            WarrantyStatus.ACTIVE -> "Active" to Color(0xFF2E7D32)
                            WarrantyStatus.EXPIRING_SOON -> "Expires in $daysRemaining days" to Color(0xFFE65100)
                            WarrantyStatus.EXPIRED -> "Expired" to Color(0xFFC62828)
                            WarrantyStatus.NONE -> "No Warranty Data" to MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (item.warrantyEndDate != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailPair("Expires On", dateFormatter.format(Date(item.warrantyEndDate)))

                        if (!item.warrantyProvider.isNullOrBlank()) {
                            DetailPair("Provider", item.warrantyProvider)
                        }
                    }
                }
            }

            // ─────────────────────────────
            // FINANCIAL SPENDING SUMMARY
            // ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Investment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val purchase = item.purchasePrice ?: 0.0
                    val maintenance = item.maintenanceCost ?: 0.0
                    val repair = item.repairCost ?: 0.0
                    val total = purchase + maintenance + repair

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "₹${"%.2f".format(total)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailPair("Purchase", if (purchase > 0) "₹${purchase.toInt()}" else "N/A")
                        DetailPair("Maintenance", if (maintenance > 0) "₹${maintenance.toInt()}" else "₹0")
                        DetailPair("Repairs", if (repair > 0) "₹${repair.toInt()}" else "₹0")
                    }
                }
            }

            // ─────────────────────────────
            // OVERVIEW & SPECS
            // ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Product Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!item.subCategory.isNullOrBlank()) DetailPair("Subcategory", item.subCategory)
                    if (!item.serialNumber.isNullOrBlank()) DetailPair("Serial Number", item.serialNumber)
                    if (!item.barcode.isNullOrBlank()) DetailPair("Barcode", item.barcode)
                    if (!item.room.isNullOrBlank()) DetailPair("Room", item.room)
                    if (!item.condition.isNullOrBlank()) DetailPair("Condition", item.condition)
                    if (item.purchaseDate != null) DetailPair("Purchase Date", dateFormatter.format(Date(item.purchaseDate)))
                    if (!item.sellerStore.isNullOrBlank()) DetailPair("Purchased From", item.sellerStore)
                    if (!item.notes.isNullOrBlank()) DetailPair("Notes", item.notes)
                }
            }

            // ─────────────────────────────
            // LINKED DOCUMENTS
            // ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Linked Documents (${linkedDocuments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (linkedDocuments.isEmpty()) {
                        Text(
                            text = "No documents linked to this item yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            linkedDocuments.forEach { doc ->
                                LinkedDocumentRow(doc = doc, onClick = { onDocumentClick(doc.id) })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DetailPair(label: String, value: String) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LinkedDocumentRow(
    doc: DocumentEntity,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = doc.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = doc.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
