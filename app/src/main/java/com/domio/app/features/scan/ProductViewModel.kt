package com.domio.app.features.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domio.app.data.remote.ProductDto
import com.domio.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val product: ProductDto? = null,
    val error: String? = null
)

class ProductViewModel : ViewModel() {

    private val repository =
        ProductRepository()

    private val _uiState =
        MutableStateFlow(ProductUiState())

    val uiState: StateFlow<ProductUiState> =
        _uiState.asStateFlow()


    fun fetchProduct(barcode: String) {

        _uiState.value =
            ProductUiState(
                isLoading = true
            )

        viewModelScope.launch {

            val result =
                repository.getProduct(barcode)

            result
                .onSuccess { product ->

                    if (product != null) {

                        _uiState.value =
                            ProductUiState(
                                isLoading = false,
                                product = product
                            )

                    } else {

                        _uiState.value =
                            ProductUiState(
                                isLoading = false,
                                error =
                                    "Product not found"
                            )
                    }
                }
                .onFailure { exception ->

                    _uiState.value =
                        ProductUiState(
                            isLoading = false,
                            error =
                                exception.message
                                    ?: "Unable to fetch product"
                        )
                }
        }
    }


    fun reset() {

        _uiState.value =
            ProductUiState()
    }
}
