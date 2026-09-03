package com.domio.app.data.repository

import com.domio.app.data.local.dao.AssetDao
import com.domio.app.data.local.entity.AssetEntity
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

    fun searchAssets(query: String): Flow<List<AssetEntity>> {
        return assetDao.search(query)
    }
}