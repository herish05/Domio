package com.domio.app.features.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.domio.app.features.scan.scanner.BarcodeAnalyzer

@Composable
fun ProductScannerScreen(
    onBack: () -> Unit,
    onBarcodeDetected: (BarcodeResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Camera Permission Needed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Domio needs access to your camera to scan product barcodes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ─────────────────────────────
        // CAMERA PREVIEW VIEW
        // ─────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(
                        ContextCompat.getMainExecutor(ctx),
                        BarcodeAnalyzer { barcode ->
                            onBarcodeDetected(barcode)
                        }
                    )

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // ─────────────────────────────
        // MODERN VIEWFINDER RETICLE OVERLAY
        // ─────────────────────────────
        ScannerOverlay()

        // ─────────────────────────────
        // TOP CONTROL BAR (BACK & FLASH)
        // ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Scan Barcode",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = {
                    val nextTorchState = !isTorchOn
                    camera?.cameraControl?.enableTorch(nextTorchState)
                    isTorchOn = nextTorchState
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White
                )
            }
        }

        // ─────────────────────────────
        // BOTTOM HELPER BANNER
        // ─────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.75f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Align product barcode within frame",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val boxWidth = width * 0.78f
        val boxHeight = boxWidth * 0.65f

        val left = (width - boxWidth) / 2f
        val top = (height - boxHeight) / 2.2f
        val right = left + boxWidth
        val bottom = top + boxHeight

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, right, bottom),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            )
        }

        // Draw translucent mask outside scan window
        clipPath(path, clipOp = ClipOp.Difference) {
            drawRect(color = Color(0x99000000))
        }

        // Draw 4 corner bracket markers
        val cornerLength = 28.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val cornerColor = Color(0xFF4CAF50)

        // Top-Left Corner
        drawLine(cornerColor, Offset(left, top + cornerLength), Offset(left, top), strokeWidth)
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)

        // Top-Right Corner
        drawLine(cornerColor, Offset(right - cornerLength, top), Offset(right, top), strokeWidth)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

        // Bottom-Left Corner
        drawLine(cornerColor, Offset(left, bottom - cornerLength), Offset(left, bottom), strokeWidth)
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)

        // Bottom-Right Corner
        drawLine(cornerColor, Offset(right - cornerLength, bottom), Offset(right, bottom), strokeWidth)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

        // Draw animated laser scanning line
        val laserY = top + (boxHeight * laserProgress)
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = 0.85f),
            start = Offset(left + 12.dp.toPx(), laserY),
            end = Offset(right - 12.dp.toPx(), laserY),
            strokeWidth = 3.dp.toPx()
        )
    }
}