package com.domio.app.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.domio.app.data.repository.AssetRepository

class AssetViewModelFactory(
    private val repository: AssetRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AssetViewModel::class.java)) {
            return AssetViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel: ${modelClass.name}"
        )
    }
}