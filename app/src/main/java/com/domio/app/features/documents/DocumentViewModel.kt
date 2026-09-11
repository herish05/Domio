package com.domio.app.features.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.data.repository.DocumentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DocumentViewModel(
    private val repository: DocumentRepository
): ViewModel() {
    val documents: StateFlow<List<DocumentEntity>> =
        repository.observeDocuments().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
    fun addDocument(
        title: String,
        type: String,
        documentNumber: String?,
        issuer: String?,
        issueDate: Long?,
        expiryDate: Long?,
        amount: Double?,
        notes: String?,
        reminderEnabled: Boolean,
        reminderDaysBefore: Int,
        fileUri: String,
        dynamicFieldsJson: String?
    ) {

        viewModelScope.launch {

            val now = System.currentTimeMillis()

            val document = DocumentEntity(
                id = UUID.randomUUID().toString(),

                title = title.trim(),

                type = type,

                fileUri = fileUri,

                documentNumber =
                    documentNumber
                        ?.trim()
                        ?.ifBlank { null },

                issuer =
                    issuer
                        ?.trim()
                        ?.ifBlank { null },

                issueDate = issueDate,

                expiryDate = expiryDate,

                amount = amount,
                
                dynamicFields = dynamicFieldsJson,

                notes =
                    notes
                        ?.trim()
                        ?.ifBlank { null },

                reminderEnabled = reminderEnabled,

                reminderDaysBefore = reminderDaysBefore,

                createdAt = now,

                updatedAt = now
            )

            repository.addDocument(document)
        }
    }
    
    suspend fun getDocument(id: String): DocumentEntity? {
        return repository.getDocument(id)
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(documentId)
            if (doc != null) {
                repository.deleteDocument(doc)
            }
        }
    }
}