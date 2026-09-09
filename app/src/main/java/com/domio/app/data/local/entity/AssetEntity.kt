package com.domio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "assets")
data class AssetEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,

    val category: String,

    val subCategory: String? = null,

    val brand: String? = null,

    val model: String? = null,

    val barcode: String? = null,

    val serialNumber: String? = null,

    val location: String? = null,

    val room: String? = null,

    val condition: String? = null,

    val notes: String? = null,

    val purchaseDate: Long? = null,

    val purchasePrice: Double? = null,

    val currency: String = "INR",

    val sellerStore: String? = null,

    val receiptInfo: String? = null,

    val warrantyProvider: String? = null,

    val warrantyStartDate: Long? = null,

    val warrantyEndDate: Long? = null,

    val warrantyDurationMonths: Int? = null,

    val maintenanceCost: Double? = null,

    val repairCost: Double? = null,

    val estimatedCurrentValue: Double? = null,

    val imageUri: String? = null,

    val isArchived: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)