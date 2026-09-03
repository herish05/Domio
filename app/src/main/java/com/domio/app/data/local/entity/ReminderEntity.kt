package com.domio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(

    @PrimaryKey
    val id: String,

    val title: String,

    val description: String? = null,

    val dueDate: Long,

    val relatedAssetId: String? = null,

    val relatedDocumentId: String? = null,

    val isCompleted: Boolean = false,

    val createdAt: Long
)