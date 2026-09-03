package com.domio.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /*
     * Android Emulator:
     *
     * 10.0.2.2 = your computer's localhost
     *
     * Our Node.js server runs on:
     * localhost:5000
     */
    private const val BASE_URL =
        "http://127.0.0.1:5000/"

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {

            level =
                HttpLoggingInterceptor.Level.BODY
        }

    private val httpClient =
        OkHttpClient.Builder()

            .addInterceptor(
                loggingInterceptor
            )

            .connectTimeout(
                10,
                TimeUnit.SECONDS
            )

            .readTimeout(
                10,
                TimeUnit.SECONDS
            )

            .writeTimeout(
                10,
                TimeUnit.SECONDS
            )

            .build()


    val productApi: ProductApi by lazy {

        Retrofit.Builder()

            .baseUrl(BASE_URL)

            .client(httpClient)

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(ProductApi::class.java)
    }
}