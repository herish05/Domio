package com.domio.app.features.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.core.util.ExportAction
import com.domio.app.core.util.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportFormatDialog(
    initialAction: ExportAction = ExportAction.SHARE,
    onDismiss: () -> Unit,
    onConfirm: (ExportFormat, ExportAction) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var selectedAction by remember { mutableStateOf(initialAction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (selectedAction == ExportAction.SHARE) "Share Document" else "Download Document",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your preferred document export format:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // FORMAT OPTIONS
                ExportFormat.values().forEach { format ->
                    val isSelected = selectedFormat == format
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = format.displayName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (format) {
                                        ExportFormat.PDF -> "High-quality PDF document layout"
                                        ExportFormat.JPG -> "High-res compressed image"
                                        ExportFormat.PNG -> "Lossless image with high clarity"
                                        ExportFormat.TXT -> "Plain text document summary"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ACTION SELECTOR (SHARE vs DOWNLOAD)
                Text(
                    text = "Export Action:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedAction == ExportAction.SHARE,
                        onClick = { selectedAction = ExportAction.SHARE },
                        label = { Text("Share to Apps") },
                        leadingIcon = { Icon(Icons.Outlined.Share, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedAction == ExportAction.DOWNLOAD,
                        onClick = { selectedAction = ExportAction.DOWNLOAD },
                        label = { Text("Save to Phone") },
                        leadingIcon = { Icon(Icons.Outlined.Download, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedFormat, selectedAction) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (selectedAction == ExportAction.SHARE) Icons.Outlined.Share else Icons.Outlined.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedAction == ExportAction.SHARE) "Export & Share" else "Save File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
