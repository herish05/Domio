package com.domio.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "asset_document_cross_ref",
    primaryKeys = ["assetId", "documentId"]
)
data class AssetDocumentCrossRef(

    val assetId: String,

    val documentId: String
)