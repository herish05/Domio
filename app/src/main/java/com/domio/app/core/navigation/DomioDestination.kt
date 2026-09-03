package com.domio.app.core.navigation

sealed class DomioDestination(
    val route: String
) {

    data object Home : DomioDestination("home")

    data object Things : DomioDestination("things")

    data object Scan : DomioDestination("scan")

    data object Documents : DomioDestination("documents")

    data object AddAsset : DomioDestination("add_asset")

    data object ManualAsset : DomioDestination("manual_asset")

    data object AddDocument : DomioDestination("add_document")
    
    data object DocumentDetails : DomioDestination("document_details/{documentId}") {
        fun createRoute(documentId: String) = "document_details/$documentId"
    }

    data object Activity : DomioDestination("activity")

    data object ProductScanner : DomioDestination("product_scanner")

    data object ProductReview : DomioDestination("product_review")
}