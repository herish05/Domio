package com.domio.app.features.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.domio.app.data.local.entity.DocumentEntity

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            
            if (selectedCategory == null) {
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Important documents, always easy to find.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedCategory = null }, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (selectedCategory == "All") "All Documents" else selectedCategory ?: "",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                },
                placeholder = { Text("Search documents") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (filteredDocuments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No documents found", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Try adjusting your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (selectedCategory == null && search.isBlank()) {
                // Show Category Grid
                val categories = documents.map { it.type.ifBlank { "Other" } }.distinct().sorted()
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No documents in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                     }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add document")
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
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
            
            Column {
                Text(
                    text = if (name == "All") "All Documents" else name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)
            ) {
                Text(text = document.title, style = MaterialTheme.typography.titleMedium)
                Text(text = document.type, color = MaterialTheme.colorScheme.onSurfaceVariant)
                document.issuer?.let { issuer ->
                    Text(text = issuer, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}