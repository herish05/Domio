package com.domio.app.core.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.domio.app.data.local.entity.DocumentEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF("pdf", "application/pdf", "PDF Document (.pdf)"),
    JPG("jpg", "image/jpeg", "JPEG Image (.jpg)"),
    PNG("png", "image/png", "PNG Image (.png)"),
    TXT("txt", "text/plain", "Text Summary (.txt)")
}

enum class ExportAction {
    SHARE, DOWNLOAD
}

object DocumentConverter {

    fun exportDocument(
        context: Context,
        document: DocumentEntity,
        format: ExportFormat,
        action: ExportAction
    ) {
        try {
            val exportedFile = when (format) {
                ExportFormat.PDF -> generatePdf(context, document)
                ExportFormat.JPG -> generateImage(context, document, Bitmap.CompressFormat.JPEG, "jpg")
                ExportFormat.PNG -> generateImage(context, document, Bitmap.CompressFormat.PNG, "png")
                ExportFormat.TXT -> generateTextSummary(context, document)
            }

            if (exportedFile == null || !exportedFile.exists()) {
                Toast.makeText(context, "Failed to generate ${format.extension.uppercase()} file", Toast.LENGTH_SHORT).show()
                return
            }

            when (action) {
                ExportAction.DOWNLOAD -> saveToDownloads(context, exportedFile, format)
                ExportAction.SHARE -> shareFile(context, exportedFile, format)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generatePdf(context: Context, document: DocumentEntity): File? {
        val outputDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "Domio_${sanitizeFilename(document.title)}_${System.currentTimeMillis()}.pdf")

        // 1. If original file is already a PDF, copy it directly for 100% clean original printable PDF
        if (document.fileUri.isNotBlank() && isPdfUri(context, document.fileUri)) {
            val inputStream = context.contentResolver.openInputStream(document.fileUri.toUri())
            if (inputStream != null) {
                FileOutputStream(outputFile).use { out -> inputStream.copyTo(out) }
                inputStream.close()
                return outputFile
            }
        }

        // 2. If original file is an image, embed the clean document image into a standard printable A4 PDF page without adding text overlays
        if (document.fileUri.isNotBlank()) {
            val bitmap = loadBitmapFromUri(context, document.fileUri)
            if (bitmap != null) {
                val pdfDoc = PdfDocument()
                
                // Standard A4 Size in points: 595 x 842
                val pageWidth = 595
                val pageHeight = 842
                val margin = 20f

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Pure white printable background
                canvas.drawColor(Color.WHITE)

                val availableWidth = pageWidth - (margin * 2)
                val availableHeight = pageHeight - (margin * 2)

                val scale = Math.min(availableWidth / bitmap.width, availableHeight / bitmap.height)
                val scaledWidth = (bitmap.width * scale).toInt()
                val scaledHeight = (bitmap.height * scale).toInt()

                val left = margin + (availableWidth - scaledWidth) / 2f
                val top = margin + (availableHeight - scaledHeight) / 2f

                if (scaledWidth > 0 && scaledHeight > 0) {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                    canvas.drawBitmap(scaledBitmap, left, top, null)
                }

                pdfDoc.finishPage(page)
                pdfDoc.writeTo(FileOutputStream(outputFile))
                pdfDoc.close()
                return outputFile
            }
        }

        // 3. If no image attached, generate a clean printable document summary page
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
        }

        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 12f
            isFakeBoldText = true
        }

        var y = 50f
        canvas.drawText(document.title.uppercase(Locale.getDefault()), 40f, y, titlePaint)
        y += 20f
        canvas.drawText("DOCUMENT SUMMARY RECORD", 40f, y, labelPaint)
        y += 20f

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        fun drawField(label: String, value: String) {
            canvas.drawText(label, 40f, y, labelPaint)
            canvas.drawText(value, 180f, y, paint)
            y += 22f
        }

        drawField("Document Name:", document.title)
        drawField("Document Type:", document.type)
        document.documentNumber?.takeIf { it.isNotBlank() }?.let { drawField("Document Number:", it) }
        document.issuer?.takeIf { it.isNotBlank() }?.let { drawField("Issued By / Store:", it) }
        document.amount?.let { drawField("Total Amount:", "₹${it.toInt()}") }
        document.issueDate?.let { drawField("Issue Date:", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))) }
        document.expiryDate?.let { drawField("Expiry Date:", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))) }

        if (!document.dynamicFields.isNullOrBlank()) {
            try {
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val fields: Map<String, String>? = Gson().fromJson(document.dynamicFields, mapType)
                fields?.filterValues { it.isNotBlank() }?.forEach { (key, valStr) ->
                    drawField("$key:", valStr)
                }
            } catch (_: Exception) {}
        }

        if (!document.notes.isNullOrBlank()) {
            drawField("Notes:", document.notes)
        }

        pdfDoc.finishPage(page)
        pdfDoc.writeTo(FileOutputStream(outputFile))
        pdfDoc.close()
        return outputFile
    }

    private fun generateImage(context: Context, document: DocumentEntity, format: Bitmap.CompressFormat, ext: String): File? {
        val outputDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "Domio_${sanitizeFilename(document.title)}_${System.currentTimeMillis()}.$ext")

        var bitmap: Bitmap? = null

        if (document.fileUri.isNotBlank()) {
            if (isPdfUri(context, document.fileUri)) {
                bitmap = renderPdfPageToBitmap(context, document.fileUri)
            } else {
                bitmap = loadBitmapFromUri(context, document.fileUri)
            }
        }

        if (bitmap == null) {
            // Generate synthetic bitmap card from document details
            bitmap = generateBitmapFromDetails(document)
        }

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(format, 95, out)
        }

        return outputFile
    }

    private fun generateTextSummary(context: Context, document: DocumentEntity): File? {
        val outputDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "Domio_${sanitizeFilename(document.title)}_${System.currentTimeMillis()}.txt")

        val sb = StringBuilder()
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        sb.appendLine("==========================================")
        sb.appendLine("DOMIO DOCUMENT VAULT SUMMARY")
        sb.appendLine("==========================================")
        sb.appendLine("Title:           ${document.title}")
        sb.appendLine("Category / Type: ${document.type}")
        document.documentNumber?.takeIf { it.isNotBlank() }?.let { sb.appendLine("Document No:     $it") }
        document.issuer?.takeIf { it.isNotBlank() }?.let { sb.appendLine("Issuer / Store:  $it") }
        document.amount?.let { sb.appendLine("Amount:          ₹${it.toInt()}") }
        document.issueDate?.let { sb.appendLine("Issue Date:      ${dateFormatter.format(Date(it))}") }
        document.expiryDate?.let { sb.appendLine("Expiry Date:     ${dateFormatter.format(Date(it))}") }

        if (!document.dynamicFields.isNullOrBlank()) {
            sb.appendLine("------------------------------------------")
            sb.appendLine("EXTRACTED DETAILS")
            sb.appendLine("------------------------------------------")
            try {
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val fields: Map<String, String>? = Gson().fromJson(document.dynamicFields, mapType)
                fields?.filterValues { it.isNotBlank() }?.forEach { (key, valStr) ->
                    sb.appendLine("$key: $valStr")
                }
            } catch (_: Exception) {}
        }

        if (!document.notes.isNullOrBlank()) {
            sb.appendLine("------------------------------------------")
            sb.appendLine("NOTES")
            sb.appendLine("------------------------------------------")
            sb.appendLine(document.notes)
        }

        sb.appendLine("==========================================")

        outputFile.writeText(sb.toString())
        return outputFile
    }

    private fun saveToDownloads(context: Context, file: File, format: ExportFormat) {
        val fileName = file.name
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (format == ExportFormat.JPG || format == ExportFormat.PNG) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }

            val relativePath = if (format == ExportFormat.JPG || format == ExportFormat.PNG) {
                Environment.DIRECTORY_PICTURES + "/Domio"
            } else {
                Environment.DIRECTORY_DOWNLOADS + "/Domio"
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }

            val itemUri = resolver.insert(collection, values)
            if (itemUri != null) {
                resolver.openOutputStream(itemUri)?.use { out ->
                    file.inputStream().use { input -> input.copyTo(out) }
                }
                Toast.makeText(context, "Saved to $relativePath/$fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } else {
            val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domio").apply { if (!exists()) mkdirs() }
            val targetFile = File(targetDir, fileName)
            file.copyTo(targetFile, overwrite = true)
            Toast.makeText(context, "Saved to ${targetFile.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(context: Context, file: File, format: ExportFormat) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Document via..."))
    }

    private fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = uriString.toUri()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun renderPdfPageToBitmap(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = uriString.toUri()
            val fileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
            if (fileDescriptor != null) {
                val renderer = PdfRenderer(fileDescriptor)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    fileDescriptor.close()
                    return bitmap
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateBitmapFromDetails(document: DocumentEntity): Bitmap {
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#FFF5F7")) // Light Fantastic Pink tint

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#E91E63")
            textSize = 42f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText(document.title, 50f, 100f, titlePaint)
        canvas.drawText("Category: ${document.type}", 50f, 180f, paint)
        document.documentNumber?.let { canvas.drawText("Doc #: $it", 50f, 240f, paint) }
        document.issuer?.let { canvas.drawText("Issuer: $it", 50f, 300f, paint) }
        document.amount?.let { canvas.drawText("Amount: ₹${it.toInt()}", 50f, 360f, paint) }

        return bitmap
    }

    fun isPdfUri(context: Context, uriString: String): Boolean {
        if (uriString.endsWith(".pdf", ignoreCase = true)) return true
        return try {
            val type = context.contentResolver.getType(uriString.toUri())
            type?.contains("pdf", ignoreCase = true) == true
        } catch (_: Exception) {
            false
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
    }
}
