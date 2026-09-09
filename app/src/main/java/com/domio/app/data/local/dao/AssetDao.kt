package com.domio.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.domio.app.data.local.entity.AssetDocumentCrossRef
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity)

    @Update
    suspend fun update(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AssetEntity?

    @Query(
        """
        SELECT * FROM assets
        WHERE (name LIKE '%' || :query || '%'
        OR brand LIKE '%' || :query || '%'
        OR model LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR barcode LIKE '%' || :query || '%'
        OR serialNumber LIKE '%' || :query || '%')
        AND isArchived = 0
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE category = :category AND isArchived = 0 ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<AssetEntity>>

    @Query("UPDATE assets SET isArchived = 1, updatedAt = :now WHERE id = :id")
    suspend fun archive(id: String, now: Long = System.currentTimeMillis())

    // Linked Document Relationships
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetDocumentCrossRef(crossRef: AssetDocumentCrossRef)

    @Delete
    suspend fun deleteAssetDocumentCrossRef(crossRef: AssetDocumentCrossRef)

    @Query(
        """
        SELECT d.* FROM documents d
        INNER JOIN asset_document_cross_ref ref ON d.id = ref.documentId
        WHERE ref.assetId = :assetId
        ORDER BY d.createdAt DESC
        """
    )
    fun getDocumentsForAsset(assetId: String): Flow<List<DocumentEntity>>
}