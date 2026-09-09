package com.domio.app.features.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.domio.app.ui.components.ThemeToggleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    assets: List<AssetEntity> = emptyList(),
    onAddAsset: () -> Unit = {},
    onScanProduct: () -> Unit = {},
    onThingClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val categoriesList = listOf("All") + AssetCategories.CATEGORIES.map { it.name }

    val filteredAssets = remember(assets, searchQuery, selectedCategory, selectedStatusFilter) {
        assets.filter { asset ->
            val matchesCategory = if (selectedCategory == "All") true else asset.category.equals(selectedCategory, ignoreCase = true)

            val matchesSearch = if (searchQuery.isBlank()) true else {
                asset.name.contains(searchQuery, ignoreCase = true) ||
                        asset.brand.orEmpty().contains(searchQuery, ignoreCase = true) ||
                        asset.model.orEmpty().contains(searchQuery, ignoreCase = true) ||
                        asset.category.contains(searchQuery, ignoreCase = true) ||
                        asset.barcode.orEmpty().contains(searchQuery, ignoreCase = true) ||
                        asset.serialNumber.orEmpty().contains(searchQuery, ignoreCase = true)
            }

            val status = AssetCategories.calculateWarrantyStatus(asset.warrantyEndDate)
            val matchesStatus = when (selectedStatusFilter) {
                "Expiring Soon" -> status == WarrantyStatus.EXPIRING_SOON
                "Active Warranty" -> status == WarrantyStatus.ACTIVE
                "Expired" -> status == WarrantyStatus.EXPIRED
                else -> true
            }

            matchesCategory && matchesSearch && matchesStatus
        }
    }

    val totalInvestment = remember(assets) {
        assets.sumOf { (it.purchasePrice ?: 0.0) + (it.maintenanceCost ?: 0.0) + (it.repairCost ?: 0.0) }
    }

    val expiringCount = remember(assets) {
        assets.count { AssetCategories.calculateWarrantyStatus(it.warrantyEndDate) == WarrantyStatus.EXPIRING_SOON }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAsset,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Thing")
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
                    Column {
                        Text(
                            text = "Things",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Everything you own in your home",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    ThemeToggleIconButton()
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SUMMARY DASHBOARD HEADER CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Inventory",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${assets.size} Things",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (totalInvestment > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Investment",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${"%.0f".format(totalInvestment)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    placeholder = { Text("Search by name, brand, model, barcode...") },
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CATEGORY FILTER CHIPS
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesList) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (category != "All") {
                                { Text(AssetCategories.getCategoryEmoji(category)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (expiringCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == "Expiring Soon") "All" else "Expiring Soon"
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF9100).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$expiringCount warranties expiring soon",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFF9100),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CONTENT LIST WITH SUFFICIENT BOTTOM PADDING SO NAVIGATION BAR NEVER BLOCKS ITEMS
                if (filteredAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (assets.isEmpty()) "Your Things live here" else "No matching items found",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (assets.isEmpty()) "Add the things you own and let Domio help you manage, protect, and track them." else "Try adjusting your search query or filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (assets.isEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = onScanProduct,
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan Product")
                                    }
                                    OutlinedButton(
                                        onClick = onAddAsset,
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Add Manually")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp), // Fix bottom bar cutoff
                        modifier = Modifier.weight(1f)
                    ) {
                        items(items = filteredAssets, key = { it.id }) { asset ->
                            ThingDashboardCard(
                                asset = asset,
                                onClick = { onThingClick(asset.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThingDashboardCard(
    asset: AssetEntity,
    onClick: () -> Unit
) {
    val emoji = AssetCategories.getCategoryEmoji(asset.category)
    val warrantyStatus = AssetCategories.calculateWarrantyStatus(asset.warrantyEndDate)
    val daysRemaining = AssetCategories.getDaysRemaining(asset.warrantyEndDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(54.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!asset.brand.isNullOrBlank() || !asset.model.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(asset.brand, asset.model).joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!asset.location.isNullOrBlank()) {
                    Text(
                        text = "📍 ${asset.location}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (warrantyStatus != WarrantyStatus.NONE) {
                    val (statusText, statusColor) = when (warrantyStatus) {
                        WarrantyStatus.ACTIVE -> "Warranty" to Color(0xFF00E676)
                        WarrantyStatus.EXPIRING_SOON -> "$daysRemaining days left" to Color(0xFFFF9100)
                        WarrantyStatus.EXPIRED -> "Expired" to Color(0xFFFF1744)
                        else -> "" to Color.Gray
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (asset.purchasePrice != null && asset.purchasePrice > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${asset.purchasePrice.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}