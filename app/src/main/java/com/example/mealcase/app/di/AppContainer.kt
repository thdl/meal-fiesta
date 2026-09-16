package com.example.mealcase.app.di

import com.example.mealcase.BuildConfig
import com.example.mealcase.core.data.DefaultMealRepository
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.network.MealDbApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

/**
 * Manual dependency injection: one object, owned by the [com.example.mealcase.app.MealCaseApplication],
 * that builds the object graph lazily.
 *
 * Hilt would be the obvious choice in a larger app, and the seam is here if it is ever
 * wanted — ViewModels take a [MealRepository], not a container. For three screens and one
 * data source, though, a few lines of `by lazy` cost nothing, generate nothing, and can be
 * read top to bottom.
 */
class AppContainer {

    private val json = Json {
        // TheMealDB returns considerably more fields than we model.
        ignoreUnknownKeys = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                // Request and response bodies must never be logged in a release build.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            // First-letter discovery returns full recipe bodies; avoid dumping
                            // megabytes of upstream data into Logcat during every cold start.
                            level = HttpLoggingInterceptor.Level.BASIC
                        },
                    )
                }
            }
            .build()
    }

    private val api: MealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(MealDbApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }

    val mealRepository: MealRepository by lazy { DefaultMealRepository(api) }
}
