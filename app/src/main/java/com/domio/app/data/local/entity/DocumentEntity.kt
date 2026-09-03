package com.domio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "documents")
data class DocumentEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    val type: String,

    val fileUri: String = "",

    val documentNumber: String? = null,

    val issuer: String? = null,

    val issueDate: Long? = null,

    val expiryDate: Long? = null,

    val amount: Double? = null,

    val notes: String? = null,

    val reminderEnabled: Boolean = false,

    val reminderDaysBefore: Int = 30,

    val dynamicFields: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)