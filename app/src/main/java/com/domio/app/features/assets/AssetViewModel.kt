package com.domio.app.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.data.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AssetViewModel(
    private val repository: AssetRepository
) : ViewModel() {

    val assets: StateFlow<List<AssetEntity>> =
        repository
            .observeAssets()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addAsset(
        name: String,
        category: String,
        subCategory: String? = null,
        brand: String? = null,
        model: String? = null,
        barcode: String? = null,
        serialNumber: String? = null,
        location: String? = null,
        room: String? = null,
        condition: String? = null,
        notes: String? = null,
        purchaseDate: Long? = null,
        purchasePrice: Double? = null,
        sellerStore: String? = null,
        warrantyProvider: String? = null,
        warrantyStartDate: Long? = null,
        warrantyEndDate: Long? = null,
        maintenanceCost: Double? = null,
        repairCost: Double? = null,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            val asset = AssetEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                category = category.ifBlank { "Other Belongings" },
                subCategory = subCategory?.trim()?.ifBlank { null },
                brand = brand?.trim()?.ifBlank { null },
                model = model?.trim()?.ifBlank { null },
                barcode = barcode?.trim()?.ifBlank { null },
                serialNumber = serialNumber?.trim()?.ifBlank { null },
                location = location?.trim()?.ifBlank { null },
                room = room?.trim()?.ifBlank { null },
                condition = condition?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                purchaseDate = purchaseDate,
                purchasePrice = purchasePrice,
                currency = "INR",
                sellerStore = sellerStore?.trim()?.ifBlank { null },
                warrantyProvider = warrantyProvider?.trim()?.ifBlank { null },
                warrantyStartDate = warrantyStartDate,
                warrantyEndDate = warrantyEndDate,
                maintenanceCost = maintenanceCost,
                repairCost = repairCost,
                imageUri = imageUri,
                createdAt = now,
                updatedAt = now
            )

            repository.addAsset(asset)
        }
    }

    suspend fun getAsset(id: String): AssetEntity? {
        return repository.getAsset(id)
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun archiveAsset(id: String) {
        viewModelScope.launch {
            repository.archiveAsset(id)
        }
    }

    fun linkDocument(assetId: String, documentId: String) {
        viewModelScope.launch {
            repository.linkDocumentToAsset(assetId, documentId)
        }
    }

    fun getDocumentsForAsset(assetId: String): Flow<List<DocumentEntity>> {
        return repository.getDocumentsForAsset(assetId)
    }
}