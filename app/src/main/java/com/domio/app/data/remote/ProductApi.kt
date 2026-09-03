package com.domio.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {

    @GET("api/products/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): ProductResponse
}