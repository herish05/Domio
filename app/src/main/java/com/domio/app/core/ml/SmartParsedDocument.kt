package com.domio.app.core.ml

data class SmartParsedDocument(
    val fileUri: String,
    val extractedAmount: Double? = null,
    val extractedDate: Long? = null,
    val extractedExpiryDate: Long? = null,
    val extractedIssuer: String? = null,
    val extractedDocumentNumber: String? = null,
    val extractedTitle: String? = null,
    val extractedType: String? = null,
    val extractedDynamicFields: Map<String, String>? = null
)
