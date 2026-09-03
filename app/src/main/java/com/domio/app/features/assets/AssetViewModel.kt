package com.domio.app.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.repository.AssetRepository
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
        brand: String,
        model: String,
        location: String
    ) {

        viewModelScope.launch {

            val now = System.currentTimeMillis()

            val asset = AssetEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                category = "OTHER",
                brand = brand.trim().ifBlank { null },
                model = model.trim().ifBlank { null },
                location = location.trim().ifBlank { null },
                createdAt = now,
                updatedAt = now
            )

            repository.addAsset(asset)
        }
    }
}