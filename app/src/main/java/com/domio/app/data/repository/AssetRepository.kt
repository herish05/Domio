package com.domio.app.data.repository

import com.domio.app.data.local.dao.AssetDao
import com.domio.app.data.local.entity.AssetDocumentCrossRef
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

class AssetRepository(
    private val assetDao: AssetDao
) {

    fun observeAssets(): Flow<List<AssetEntity>> {
        return assetDao.observeAll()
    }

    suspend fun getAsset(id: String): AssetEntity? {
        return assetDao.getById(id)
    }

    suspend fun addAsset(asset: AssetEntity) {
        assetDao.insert(asset)
    }

    suspend fun updateAsset(asset: AssetEntity) {
        assetDao.update(asset)
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        assetDao.delete(asset)
    }

    suspend fun archiveAsset(id: String) {
        assetDao.archive(id)
    }

    fun searchAssets(query: String): Flow<List<AssetEntity>> {
        return assetDao.search(query)
    }

    fun getByCategory(category: String): Flow<List<AssetEntity>> {
        return assetDao.getByCategory(category)
    }

    suspend fun linkDocumentToAsset(assetId: String, documentId: String) {
        assetDao.insertAssetDocumentCrossRef(AssetDocumentCrossRef(assetId, documentId))
    }

    suspend fun unlinkDocumentFromAsset(assetId: String, documentId: String) {
        assetDao.deleteAssetDocumentCrossRef(AssetDocumentCrossRef(assetId, documentId))
    }

    fun getDocumentsForAsset(assetId: String): Flow<List<DocumentEntity>> {
        return assetDao.getDocumentsForAsset(assetId)
    }
}