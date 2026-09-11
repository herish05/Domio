package com.domio.app.features.documents

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.domio.app.core.util.DocumentConverter
import java.io.InputStream

@Composable
fun DocumentViewerDialog(
    fileUriString: String,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPdf = remember(fileUriString) { DocumentConverter.isPdfUri(context, fileUriString) }

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    LaunchedEffect(fileUriString) {
        if (fileUriString.isNotBlank()) {
            try {
                val uri = fileUriString.toUri()
                if (isPdf) {
                    val fd = context.contentResolver.openFileDescriptor(uri, "r")
                    if (fd != null) {
                        fileDescriptor = fd
                        val renderer = PdfRenderer(fd)
                        pdfRenderer = renderer
                        pageCount = renderer.pageCount
                        if (renderer.pageCount > 0) {
                            renderPdfPage(renderer, 0) { bitmap ->
                                currentBitmap = bitmap
                            }
                        }
                    }
                } else {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        currentBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isPdf && pageCount > 0) {
                            Text(
                                text = "PDF Document • Page ${currentPageIndex + 1} of $pageCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Viewer")
                    }
                }
            },
            bottomBar = {
                if (isPdf && pageCount > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentPageIndex > 0) {
                                        currentPageIndex--
                                        pdfRenderer?.let { renderer ->
                                            renderPdfPage(renderer, currentPageIndex) { bitmap ->
                                                currentBitmap = bitmap
                                            }
                                        }
                                    }
                                },
                                enabled = currentPageIndex > 0
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                            }

                            Text(
                                text = "Page ${currentPageIndex + 1} / $pageCount",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (currentPageIndex < pageCount - 1) {
                                        currentPageIndex++
                                        pdfRenderer?.let { renderer ->
                                            renderPdfPage(renderer, currentPageIndex) { bitmap ->
                                                currentBitmap = bitmap
                                            }
                                        }
                                    }
                                },
                                enabled = currentPageIndex < pageCount - 1
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Page")
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "Document Page",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun renderPdfPage(renderer: PdfRenderer, index: Int, onRendered: (Bitmap) -> Unit) {
    try {
        val page = renderer.openPage(index)
        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        onRendered(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
