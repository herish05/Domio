package com.domio.app.data.repository

import com.domio.app.data.local.dao.DocumentDao
import com.domio.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: DocumentDao
) {

    fun observeDocuments(): Flow<List<DocumentEntity>> {
        return documentDao.observeAll()
    }

    suspend fun getDocument(
        id: String
    ): DocumentEntity? {
        return documentDao.getById(id)
    }

    suspend fun addDocument(
        document: DocumentEntity
    ) {
        documentDao.insert(document)
    }

    suspend fun updateDocument(
        document: DocumentEntity
    ) {
        documentDao.update(document)
    }

    suspend fun deleteDocument(
        document: DocumentEntity
    ) {
        documentDao.delete(document)
    }

    fun searchDocuments(
        query: String
    ): Flow<List<DocumentEntity>> {
        return documentDao.search(query)
    }

    fun getExpiringDocuments(
        date: Long
    ): Flow<List<DocumentEntity>> {
        return documentDao.getExpiringDocuments(date)
    }
}   