package com.domio.app.domain.model

data class Asset(
    val id: String,

    val name: String,

    val category: AssetCategory,

    val brand: String? = null,

    val model: String? = null,

    val serialNumber: String? = null,

    val location: String? = null,

    val purchaseDate: Long? = null,

    val purchasePrice: Double? = null,

    val warrantyStartDate: Long? = null,

    val warrantyEndDate: Long? = null,

    val imageUri: String? = null,

    val createdAt: Long,

    val updatedAt: Long
)

enum class AssetCategory {

    APPLIANCE,

    ELECTRONICS,

    VEHICLE,

    FURNITURE,

    GADGET,

    HOME_EQUIPMENT,

    OTHER
}