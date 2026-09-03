package com.domio.app.domain.model

data class Reminder(
    val id: String,

    val title: String,

    val description: String? = null,

    val dueDate: Long,

    val relatedAssetId: String? = null,

    val relatedDocumentId: String? = null,

    val isCompleted: Boolean = false,

    val createdAt: Long
)