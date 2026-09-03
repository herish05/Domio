package com.domio.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.domio.app.core.navigation.DomioDestination
import com.domio.app.data.local.database.DatabaseProvider
import com.domio.app.data.repository.AssetRepository
import com.domio.app.data.repository.DocumentRepository
import com.domio.app.features.assets.AddAssetScreen
import com.domio.app.features.assets.AssetViewModel
import com.domio.app.features.assets.AssetViewModelFactory
import com.domio.app.features.assets.AssetsScreen
import com.domio.app.features.assets.ManualAssetScreen
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
import androidx.lifecycle.viewmodel.compose.viewModel

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)


@Composable
fun DomioApp() {

    val navController = rememberNavController()

    val context = LocalContext.current


    // ---------------------------------------------------------
    // DATABASE
    // ---------------------------------------------------------

    val database = DatabaseProvider.getDatabase(context)


    // ---------------------------------------------------------
    // REPOSITORIES
    // ---------------------------------------------------------

    val assetRepository = AssetRepository(
        database.assetDao()
    )

    val documentRepository = DocumentRepository(
        database.documentDao()
    )


    // ---------------------------------------------------------
    // VIEW MODELS
    // ---------------------------------------------------------

    val assetViewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(
            assetRepository
        )
    )

    val documentViewModel: DocumentViewModel = viewModel(
        factory = DocumentViewModelFactory(
            documentRepository
        )
    )


    // ---------------------------------------------------------
    // SCANNED BARCODE
    // ---------------------------------------------------------

    var scannedBarcode by remember {
        mutableStateOf<BarcodeResult?>(null)
    }

    var scannedDocument by remember {
        mutableStateOf<com.domio.app.core.ml.SmartParsedDocument?>(null)
    }


    // ---------------------------------------------------------
    // BOTTOM NAVIGATION
    // ---------------------------------------------------------

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
            route = DomioDestination.Scan.route,
            label = "Scan",
            icon = Icons.Outlined.QrCodeScanner
        ),

        BottomItem(
            route = DomioDestination.Documents.route,
            label = "Documents",
            icon = Icons.Outlined.Description
        )
    )


    // ---------------------------------------------------------
    // SCAFFOLD
    // ---------------------------------------------------------

    Scaffold(

        bottomBar = {

            NavigationBar {

                val currentRoute =
                    navController
                        .currentBackStackEntryAsState()
                        .value
                        ?.destination
                        ?.route


                bottomItems.forEach { item ->

                    NavigationBarItem(

                        selected =
                            currentRoute == item.route,

                        onClick = {

                            navController.navigate(
                                item.route
                            ) {

                                launchSingleTop = true

                                restoreState = true

                                popUpTo(
                                    DomioDestination.Home.route
                                ) {
                                    saveState = true
                                }
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
                                text = item.label
                            )
                        }
                    )
                }
            }
        }

    ) { _ ->


        // -----------------------------------------------------
        // NAVIGATION
        // -----------------------------------------------------

        NavHost(

            navController = navController,

            startDestination =
                DomioDestination.Home.route

        ) {


            // =================================================
            // HOME
            // =================================================

            composable(
                DomioDestination.Home.route
            ) {

                HomeScreen(
                    assets =
                        assetViewModel
                            .assets
                            .collectAsState()
                            .value
                )
            }


            // =================================================
            // THINGS
            // =================================================

            composable(
                DomioDestination.Things.route
            ) {

                AssetsScreen(

                    assets =
                        assetViewModel
                            .assets
                            .collectAsState()
                            .value,

                    onAddAsset = {

                        navController.navigate(
                            DomioDestination.AddAsset.route
                        )
                    }
                )
            }


            // =================================================
            // SCAN
            // =================================================

            composable(
                DomioDestination.Scan.route
            ) {

                ScanScreen(

                    onScanProduct = {

                        navController.navigate(
                            DomioDestination.ProductScanner.route
                        )
                    },

                    onDocumentScanned = { parsedDocument ->
                        scannedDocument = parsedDocument

                        navController.navigate(
                            DomioDestination.AddDocument.route
                        )
                    }
                )
            }


            // =================================================
            // DOCUMENTS
            // =================================================

            composable(
                DomioDestination.Documents.route
            ) {

                DocumentsScreen(

                    documents =
                        documentViewModel
                            .documents
                            .collectAsState()
                            .value,

                    onAdd = {

                        navController.navigate(
                            DomioDestination.AddDocument.route
                        )
                    },
                    
                    onDocumentClick = { documentId ->
                        navController.navigate(
                            DomioDestination.DocumentDetails.createRoute(documentId)
                        )
                    }
                )
            }
            
            // =================================================
            // DOCUMENT DETAILS
            // =================================================
            
            composable(
                route = DomioDestination.DocumentDetails.route
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId")
                if (documentId != null) {
                    com.domio.app.features.documents.DocumentDetailsScreen(
                        documentId = documentId,
                        viewModel = documentViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }


            // =================================================
            // ADD ASSET
            // =================================================

            composable(
                DomioDestination.AddAsset.route
            ) {

                AddAssetScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onScanProduct = {

                        navController.navigate(
                            DomioDestination.ProductScanner.route
                        )
                    },

                    onScanBill = {

                        // OCR will be implemented later.
                    },

                    onManual = {

                        navController.navigate(
                            DomioDestination.ManualAsset.route
                        )
                    }
                )
            }


            // =================================================
            // MANUAL ASSET
            // =================================================

            composable(
                DomioDestination.ManualAsset.route
            ) {

                ManualAssetScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onSave = {
                            name,
                            brand,
                            model,
                            location ->

                        assetViewModel.addAsset(
                            name = name,
                            brand = brand,
                            model = model,
                            location = location
                        )

                        navController.popBackStack()
                    }
                )
            }


            // =================================================
            // PRODUCT SCANNER
            // =================================================

            composable(
                DomioDestination.ProductScanner.route
            ) {

                ProductScannerScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onBarcodeDetected = { barcode ->

                        scannedBarcode = barcode

                        navController.navigate(
                            DomioDestination.ProductReview.route
                        )
                    }
                )
            }


            // =================================================
            // PRODUCT REVIEW
            // =================================================

            composable(
                DomioDestination.ProductReview.route
            ) {

                val barcode =
                    scannedBarcode

                val productViewModel:
                        ProductViewModel = viewModel()


                if (barcode != null) {

                    ProductReviewScreen(

                        barcode = barcode,

                        viewModel =
                            productViewModel,

                        onBack = {

                            navController.popBackStack()
                        },

                        onSave = {
                                name,
                                brand,
                                model ->

                            assetViewModel.addAsset(

                                name = name,

                                brand = brand,

                                model = model,

                                location = ""
                            )

                            scannedBarcode = null

                            productViewModel.reset()


                            navController.navigate(
                                DomioDestination.Things.route
                            ) {

                                popUpTo(
                                    DomioDestination.Scan.route
                                ) {

                                    inclusive = true
                                }
                            }
                        }
                    )

                } else {

                    /*
                     * No barcode available.
                     * Go back to scanner.
                     */
                    navController.popBackStack()
                }
            }


            // =================================================
            // ADD DOCUMENT
            // =================================================

            composable(
                DomioDestination.AddDocument.route
            ) {
                
                val initialData = scannedDocument

                AddDocumentScreen(
                    initialData = initialData,

                    onBack = {
                        scannedDocument = null
                        navController.popBackStack()
                    },

                    onSave = {
                            title,
                            type,
                            number,
                            issuer,
                            issueDate,
                            expiryDate,
                            amount,
                            notes,
                            reminderEnabled,
                            reminderDaysBefore,
                            fileUri,
                            dynamicFieldsJson ->

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

                        navController.popBackStack()
                    }
                )
            }
        }
    }
}