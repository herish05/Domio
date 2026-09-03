package com.domio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val category: String,

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