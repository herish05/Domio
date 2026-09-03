package com.domio.app.data.repository

import com.domio.app.data.remote.ProductDto
import com.domio.app.data.remote.RetrofitClient

class ProductRepository {

    private val api =
        RetrofitClient.productApi


    suspend fun getProduct(
        barcode: String
    ): Result<ProductDto?> {

        return try {

            val response =
                api.getProduct(barcode)


            if (response.success && response.found) {

                Result.success(
                    response.product
                )

            } else {

                Result.success(
                    null
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}