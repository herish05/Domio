package com.domio.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.domio.app.core.navigation.DomioDestination
import com.domio.app.core.security.SecurityManager
import com.domio.app.data.local.database.DatabaseProvider
import com.domio.app.data.repository.AssetRepository
import com.domio.app.data.repository.DocumentRepository
import com.domio.app.features.assets.AddAssetScreen
import com.domio.app.features.assets.AssetViewModel
import com.domio.app.features.assets.AssetViewModelFactory
import com.domio.app.features.assets.AssetsScreen
import com.domio.app.features.assets.ManualAssetScreen
import com.domio.app.features.assets.ThingDetailsScreen
import com.domio.app.features.documents.AddDocumentScreen
import com.domio.app.features.documents.DocumentViewModel
import com.domio.app.features.documents.DocumentViewModelFactory
import com.domio.app.features.documents.DocumentsScreen
import com.domio.app.features.home.HomeScreen
import com.domio.app.features.scan.BarcodeResult
import com.domio.app.features.scan.ProductReviewScreen
import com.domio.app.features.scan.ProductScannerScreen
import com.domio.app.features.scan.ScanScreen
import com.domio.app.features.scan.ProductViewModel
import com.domio.app.features.security.AppLockOverlay
import com.domio.app.features.security.DocumentVaultLockScreen
import com.domio.app.features.security.SecuritySettingsDialog

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun DomioApp(
    securityManager: SecurityManager = SecurityManager(LocalContext.current)
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // SECURITY LOCK STATES
    val isAppLocked by securityManager.isAppLocked.collectAsState()
    var showSecuritySettings by remember { mutableStateOf(false) }

    // DATABASE & REPOSITORIES
    val database = DatabaseProvider.getDatabase(context)
    val assetRepository = AssetRepository(database.assetDao())
    val documentRepository = DocumentRepository(database.documentDao())

    // VIEW MODELS
    val assetViewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(assetRepository)
    )
    val documentViewModel: DocumentViewModel = viewModel(
        factory = DocumentViewModelFactory(documentRepository)
    )

    // SCANNED STATE
    var scannedBarcode by remember { mutableStateOf<BarcodeResult?>(null) }
    var scannedDocument by remember { mutableStateOf<com.domio.app.core.ml.SmartParsedDocument?>(null) }

    // BOTTOM NAVIGATION ITEMS
    val bottomItems = listOf(
        BottomItem(
            route = DomioDestination.Home.route,
            label = "Home",
            icon = Icons.Outlined.Home
        ),
        BottomItem(
            route = DomioDestination.Things.route,
            label = "Things",
            icon = Icons.Outlined.GridView
        ),
        BottomItem(
            route = DomioDestination.Documents.route,
            label = "Documents",
            icon = Icons.Outlined.Description
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    if (isAppLocked) {
        AppLockOverlay(
            securityManager = securityManager,
            onUnlocked = { securityManager.unlockAppSession() }
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(DomioDestination.Home.route) { saveState = true }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                    )
            ) {
                NavHost(
                    navController = navController,
                    startDestination = DomioDestination.Home.route
                ) {
                    // HOME
                    composable(DomioDestination.Home.route) {
                        HomeScreen(
                            assets = assetViewModel.assets.collectAsState().value,
                            onScanProduct = { navController.navigate(DomioDestination.ProductScanner.route) },
                            onAddAsset = { navController.navigate(DomioDestination.AddAsset.route) },
                            onThingClick = { assetId ->
                                navController.navigate(DomioDestination.ThingDetails.createRoute(assetId))
                            },
                            onOpenSecuritySettings = { showSecuritySettings = true }
                        )
                    }

                    // THINGS
                    composable(DomioDestination.Things.route) {
                        AssetsScreen(
                            assets = assetViewModel.assets.collectAsState().value,
                            onAddAsset = { navController.navigate(DomioDestination.AddAsset.route) },
                            onScanProduct = { navController.navigate(DomioDestination.ProductScanner.route) },
                            onThingClick = { assetId ->
                                navController.navigate(DomioDestination.ThingDetails.createRoute(assetId))
                            }
                        )
                    }

                    // THING DETAILS
                    composable(route = DomioDestination.ThingDetails.route) { backStackEntry ->
                        val assetId = backStackEntry.arguments?.getString("assetId")
                        if (assetId != null) {
                            ThingDetailsScreen(
                                assetId = assetId,
                                viewModel = assetViewModel,
                                onBack = { navController.popBackStack() },
                                onDocumentClick = { docId ->
                                    navController.navigate(DomioDestination.DocumentDetails.createRoute(docId))
                                }
                            )
                        }
                    }

                    // SCAN
                    composable(DomioDestination.Scan.route) {
                        ScanScreen(
                            onScanProduct = { navController.navigate(DomioDestination.ProductScanner.route) },
                            onDocumentScanned = { parsedDocument ->
                                scannedDocument = parsedDocument
                                navController.navigate(DomioDestination.AddDocument.route)
                            }
                        )
                    }

                    // DOCUMENTS
                    composable(DomioDestination.Documents.route) {
                        DocumentsScreen(
                            documents = documentViewModel.documents.collectAsState().value,
                            onAdd = { navController.navigate(DomioDestination.AddDocument.route) },
                            onDocumentClick = { documentId ->
                                navController.navigate(DomioDestination.DocumentDetails.createRoute(documentId))
                            },
                            onScanDocument = { navController.navigate(DomioDestination.Scan.route) },
                            onDocumentScannedFromGallery = { parsedDocument ->
                                scannedDocument = parsedDocument
                                navController.navigate(DomioDestination.AddDocument.route)
                            }
                        )
                    }

                    // DOCUMENT DETAILS
                    composable(route = DomioDestination.DocumentDetails.route) { backStackEntry ->
                        val documentId = backStackEntry.arguments?.getString("documentId")
                        if (documentId != null) {
                            com.domio.app.features.documents.DocumentDetailsScreen(
                                documentId = documentId,
                                viewModel = documentViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ADD ASSET CHOICE
                    composable(DomioDestination.AddAsset.route) {
                        AddAssetScreen(
                            onBack = { navController.popBackStack() },
                            onScanProduct = { navController.navigate(DomioDestination.ProductScanner.route) },
                            onScanBillResult = { parsedDocument ->
                                scannedDocument = parsedDocument
                                navController.navigate(DomioDestination.AddDocument.route)
                            },
                            onManual = { navController.navigate(DomioDestination.ManualAsset.route) }
                        )
                    }

                    // MANUAL ASSET
                    composable(DomioDestination.ManualAsset.route) {
                        ManualAssetScreen(
                            onBack = { navController.popBackStack() },
                            onSave = { name, category, subCategory, brand, model, barcode, serialNumber, location, room, condition, notes, purchaseDate, purchasePrice, sellerStore, warrantyProvider, warrantyStartDate, warrantyEndDate, maintenanceCost, repairCost, imageUri ->
                                assetViewModel.addAsset(
                                    name = name,
                                    category = category,
                                    subCategory = subCategory,
                                    brand = brand,
                                    model = model,
                                    barcode = barcode,
                                    serialNumber = serialNumber,
                                    location = location,
                                    room = room,
                                    condition = condition,
                                    notes = notes,
                                    purchaseDate = purchaseDate,
                                    purchasePrice = purchasePrice,
                                    sellerStore = sellerStore,
                                    warrantyProvider = warrantyProvider,
                                    warrantyStartDate = warrantyStartDate,
                                    warrantyEndDate = warrantyEndDate,
                                    maintenanceCost = maintenanceCost,
                                    repairCost = repairCost,
                                    imageUri = imageUri
                                )
                                navController.popBackStack()
                            }
                        )
                    }

                    // PRODUCT SCANNER
                    composable(DomioDestination.ProductScanner.route) {
                        ProductScannerScreen(
                            onBack = { navController.popBackStack() },
                            onBarcodeDetected = { barcode ->
                                scannedBarcode = barcode
                                navController.navigate(DomioDestination.ProductReview.route)
                            }
                        )
                    }

                    // PRODUCT REVIEW
                    composable(DomioDestination.ProductReview.route) {
                        val barcode = scannedBarcode
                        val productViewModel: ProductViewModel = viewModel()

                        if (barcode != null) {
                            ProductReviewScreen(
                                barcode = barcode,
                                viewModel = productViewModel,
                                onBack = { navController.popBackStack() },
                                onSave = { name, brand, model ->
                                    assetViewModel.addAsset(
                                        name = name,
                                        category = "Other Belongings",
                                        brand = brand,
                                        model = model,
                                        barcode = barcode.value
                                    )
                                    scannedBarcode = null
                                    productViewModel.reset()

                                    navController.navigate(DomioDestination.Things.route) {
                                        popUpTo(DomioDestination.Scan.route) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }

                    // ADD DOCUMENT
                    composable(DomioDestination.AddDocument.route) {
                        val initialData = scannedDocument

                        AddDocumentScreen(
                            initialData = initialData,
                            onBack = {
                                scannedDocument = null
                                navController.popBackStack()
                            },
                            onSave = { title, type, number, issuer, issueDate, expiryDate, amount, notes, reminderEnabled, reminderDaysBefore, fileUri, dynamicFieldsJson ->
                                documentViewModel.addDocument(
                                    title = title,
                                    type = type,
                                    documentNumber = number,
                                    issuer = issuer,
                                    issueDate = issueDate,
                                    expiryDate = expiryDate,
                                    amount = amount,
                                    notes = notes,
                                    reminderEnabled = reminderEnabled,
                                    reminderDaysBefore = reminderDaysBefore,
                                    fileUri = fileUri,
                                    dynamicFieldsJson = dynamicFieldsJson
                                )
                                scannedDocument = null
                                navController.navigate(DomioDestination.Documents.route) {
                                    popUpTo(DomioDestination.AddDocument.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showSecuritySettings) {
            SecuritySettingsDialog(
                securityManager = securityManager,
                onDismiss = { showSecuritySettings = false }
            )
        }
    }
}