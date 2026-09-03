package com.domio.app.domain.model

data class Document(
    val id: String,

    val title: String,

    val type: DocumentType,

    val fileUri: String,

    val mimeType: String? = null,

    val documentNumber: String? = null,

    val issuer: String? = null,

    val issueDate: Long? = null,

    val expiryDate: Long? = null,

    val amount: Double? = null,

    val notes: String? = null,

    val linkedAssetIds: List<String> = emptyList(),

    val createdAt: Long,

    val updatedAt: Long
)

enum class DocumentType {

    BILL,

    INVOICE,

    WARRANTY,

    INSURANCE,

    VEHICLE_DOCUMENT,

    CERTIFICATE,

    PROPERTY_DOCUMENT,

    IDENTITY,

    EDUCATION,

    MEDICAL,

    CONTRACT,

    OTHER
}