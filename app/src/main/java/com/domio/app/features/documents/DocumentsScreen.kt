package com.domio.app.features.documents

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.ui.components.ThemeToggleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<DocumentEntity>,
    onAdd: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredDocuments = if (search.isBlank()) {
        documents
    } else {
        documents.filter { document ->
            document.title.contains(search, ignoreCase = true) ||
            document.type.contains(search, ignoreCase = true) ||
            document.issuer.orEmpty().contains(search, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Document")
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
                                text = "Documents",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Important bills, warranties, & receipts",
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

                Spacer(modifier = Modifier.height(16.dp))

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
                    placeholder = { Text("Search documents by title, store, type...") },
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            Text(text = "Scan receipts or upload documents to keep them organized.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (selectedCategory == null && search.isBlank()) {
                    // Show Category Grid
                    val categories = documents.map { it.type.ifBlank { "Other" } }.distinct().sorted()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp), // Fix bottom bar cutoff
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
                            contentPadding = PaddingValues(bottom = 120.dp), // Fix bottom bar cutoff
                            modifier = Modifier.weight(1f)
                        ) {
                            items(items = displayList, key = { it.id }) { document ->
                                DocumentCard(
                                    document = document,
                                    onClick = { onDocumentClick(document.id) }
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
    onClick: () -> Unit
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

            if (document.amount != null && document.amount > 0) {
                Text(
                    text = "₹${document.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}