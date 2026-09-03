package com.domio.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.domio.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert
    suspend fun insert(
        document: DocumentEntity
    )

    @Update
    suspend fun update(
        document: DocumentEntity
    )

    @Delete
    suspend fun delete(
        document: DocumentEntity
    )

    @Query(
        "SELECT * FROM documents ORDER BY createdAt DESC"
    )
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query(
        "SELECT * FROM documents WHERE id = :id LIMIT 1"
    )
    suspend fun getById(
        id: String
    ): DocumentEntity?

    @Query(
        """
        SELECT * FROM documents
        WHERE title LIKE '%' || :query || '%'
        OR type LIKE '%' || :query || '%'
        OR issuer LIKE '%' || :query || '%'
        OR documentNumber LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(
        query: String
    ): Flow<List<DocumentEntity>>

    @Query(
        """
        SELECT * FROM documents
        WHERE expiryDate IS NOT NULL
        AND expiryDate <= :date
        ORDER BY expiryDate ASC
        """
    )
    fun getExpiringDocuments(
        date: Long
    ): Flow<List<DocumentEntity>>
}