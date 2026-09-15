package com.example.mealcase.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    /** All cuisines. Note: most of these have no meals attached — see [filterByArea]. */
    @GET("list.php?a=list")
    suspend fun listAreas(): MealsResponse<AreaDto>

    /** Meals for one cuisine. Returns `{"meals": null}` for the majority of areas. */
    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealsResponse<MealSummaryDto>

    @GET("lookup.php")
    suspend fun lookupMeal(@Query("i") id: String): MealsResponse<MealDetailDto>

    companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}
