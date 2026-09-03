package com.domio.app.data.remote

data class ProductResponse(
    val success: Boolean,
    val found: Boolean,
    val product: ProductDto?,
    val barcode: String?,
    val reason: String?
)

data class ProductDto(
    val id: Int?,
    val barcode: String,
    val name: String?,
    val brand: String?,
    val model: String?,
    val category: String?,
    val description: String?,
    val quantity: String?,
    val imageUrl: String?,
    val source: String?,
    val confidence: String?,
    val createdAt: String?,
    val updatedAt: String?
)